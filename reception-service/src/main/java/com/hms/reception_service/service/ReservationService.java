package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.model.*;
import com.hms.reception_service.repository.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final PatientRepository patientRepository;
  private final ReservationCategoryRepository categoryRepository;
  private final ReservationStatusRepository statusRepository;
  private final ExaminationRoomRepository roomRepository;
  private final DiagnosticModalityRepository modalityRepository;
  private final DiagnosticPrescriptionRepository prescriptionRepository;
  private final HrGrpcClient hrGrpcClient;
  private final SecureRandom random = new SecureRandom();

  @Transactional
  public ReservationDTO.Response createConsultationReservation(
      ReservationDTO.CreateConsultationRequest req, UUID receptionistId) {
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException("Department ID not valid in HR: " + req.departmentId());
    }
    if (!hrGrpcClient.validatePhysician(req.practitionerId())) {
      throw new InvalidOperationException("Physician ID not valid in HR: " + req.practitionerId());
    }

    Patient patient =
        patientRepository
            .findById(req.patientId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Patient not found: " + req.patientId()));

    ReservationCategory category =
        categoryRepository
            .findByCode("CONSULTATION")
            .orElseThrow(() -> new ResourceNotFoundException("Category CONSULTATION not found"));

    ReservationStatus initialStatus =
        statusRepository
            .findByCode("PENDING_PAYMENT")
            .orElseThrow(() -> new ResourceNotFoundException("Status PENDING_PAYMENT not found"));

    // Check for conflicts
    List<Reservation> conflicts =
        reservationRepository.findConflictingPractitionerReservations(
            req.practitionerId(), req.reservationDate(), req.startTime(), req.endTime());
    if (!conflicts.isEmpty()) {
      throw new InvalidOperationException(
          "Practitioner is already booked during the requested time slot");
    }

    ExaminationRoom room = null;
    if (req.roomId() != null) {
      room =
          roomRepository
              .findById(req.roomId())
              .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + req.roomId()));
    }

    Reservation res = new Reservation();
    res.setReservationNumber(generateReservationNumber("CONS"));
    res.setPatient(patient);
    res.setCategory(category);
    res.setDepartmentId(req.departmentId());
    res.setPractitionerId(req.practitionerId());
    res.setRoom(room);
    res.setReservationDate(req.reservationDate());
    res.setStartTime(req.startTime());
    res.setEndTime(req.endTime());
    res.setStatus(initialStatus);
    res.setIsPaid(Boolean.FALSE);
    res.setReasonForVisit(req.reasonForVisit());
    res.setNotes(req.notes());
    res.setReceptionistStaffId(receptionistId);

    return mapToDTO(reservationRepository.save(res));
  }

  @Transactional
  public ReservationDTO.Response createDiagnosticReservation(
      ReservationDTO.CreateDiagnosticRequest req, UUID receptionistId) {
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException("Department ID not valid in HR: " + req.departmentId());
    }

    Patient patient =
        patientRepository
            .findById(req.patientId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Patient not found: " + req.patientId()));

    ReservationCategory category =
        categoryRepository
            .findByCode("DIAGNOSTIC_IMAGING")
            .orElseThrow(
                () -> new ResourceNotFoundException("Category DIAGNOSTIC_IMAGING not found"));

    DiagnosticModality modality =
        modalityRepository
            .findById(req.modalityId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Modality not found: " + req.modalityId()));

    ExaminationRoom room =
        roomRepository
            .findById(req.roomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + req.roomId()));

    // Check room conflict
    List<Reservation> roomConflicts =
        reservationRepository.findConflictingRoomReservations(
            req.roomId(), req.reservationDate(), req.startTime(), req.endTime());
    if (!roomConflicts.isEmpty()) {
      throw new InvalidOperationException(
          "Examination room / scanner is already booked during this time slot");
    }

    DiagnosticPrescription prescription = null;
    if (req.prescriptionId() != null) {
      prescription =
          prescriptionRepository
              .findById(req.prescriptionId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Prescription not found: " + req.prescriptionId()));
    }

    ReservationStatus initialStatus =
        statusRepository
            .findByCode("PENDING_PAYMENT")
            .orElseThrow(() -> new ResourceNotFoundException("Status PENDING_PAYMENT not found"));

    Reservation res = new Reservation();
    res.setReservationNumber(generateReservationNumber(modality.getCode()));
    res.setPatient(patient);
    res.setCategory(category);
    res.setDepartmentId(req.departmentId());
    res.setRoom(room);
    res.setPrescription(prescription);
    res.setReservationDate(req.reservationDate());
    res.setStartTime(req.startTime());
    res.setEndTime(req.endTime());
    res.setStatus(initialStatus);
    res.setIsPaid(Boolean.FALSE);
    res.setReasonForVisit(req.reasonForVisit());
    res.setNotes(req.notes());
    res.setReceptionistStaffId(receptionistId);

    DiagnosticReservationDetail detail = new DiagnosticReservationDetail();
    detail.setReservation(res);
    detail.setModality(modality);
    detail.setBodyPart(
        req.bodyPart() != null
            ? req.bodyPart()
            : (prescription != null ? prescription.getBodyPart() : "Standard"));
    detail.setContrastUsed(
        req.contrastUsed() != null
            ? req.contrastUsed()
            : (prescription != null ? prescription.getContrastRequired() : Boolean.FALSE));
    detail.setTechnicianId(req.technicianId());
    detail.setSafetyChecklistCompleted(
        req.safetyChecklistCompleted() != null ? req.safetyChecklistCompleted() : Boolean.FALSE);

    res.setDiagnosticDetail(detail);

    return mapToDTO(reservationRepository.save(res));
  }

  @Transactional
  public ReservationDTO.Response markAsPaid(UUID reservationId, UUID invoiceId) {
    Reservation res =
        reservationRepository
            .findById(reservationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Reservation not found: " + reservationId));

    ReservationStatus paidStatus =
        statusRepository
            .findByCode("PAID")
            .orElseThrow(() -> new ResourceNotFoundException("Status PAID not found"));

    res.setIsPaid(Boolean.TRUE);
    res.setStatus(paidStatus);
    res.setBillingInvoiceId(invoiceId);

    return mapToDTO(reservationRepository.save(res));
  }

  @Transactional
  public ReservationDTO.Response updateStatus(
      UUID reservationId, ReservationDTO.UpdateStatusRequest req) {
    Reservation res =
        reservationRepository
            .findById(reservationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Reservation not found: " + reservationId));

    ReservationStatus newStatus =
        statusRepository
            .findById(req.statusId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Status not found: " + req.statusId()));

    res.setStatus(newStatus);
    if (req.notes() != null && !req.notes().isBlank()) {
      res.setNotes((res.getNotes() != null ? res.getNotes() + "\n" : "") + req.notes());
    }

    if ("COMPLETED".equals(newStatus.getCode()) && res.getPrescription() != null) {
      res.getPrescription().setIsFulfilled(Boolean.TRUE);
    }

    return mapToDTO(reservationRepository.save(res));
  }

  @Transactional(readOnly = true)
  public ReservationDTO.Response getReservationById(UUID id) {
    return reservationRepository
        .findById(id)
        .map(this::mapToDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReservationDTO.Response> getReservationsByPatient(
      UUID patientId, Pageable pageable) {
    return PageResponse.from(
        reservationRepository.findByPatientId(patientId, pageable).map(this::mapToDTO));
  }

  private String generateReservationNumber(String prefix) {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    int rand = 1000 + random.nextInt(9000);
    return prefix + "-" + datePart + "-" + rand;
  }

  public ReservationDTO.Response mapToDTO(Reservation r) {
    ReservationDTO.DiagnosticDetailResponse diagDetail = null;
    if (r.getDiagnosticDetail() != null) {
      DiagnosticReservationDetail d = r.getDiagnosticDetail();
      diagDetail =
          new ReservationDTO.DiagnosticDetailResponse(
              d.getModality().getId(),
              d.getModality().getName(),
              d.getModality().getCode(),
              d.getBodyPart(),
              d.getContrastUsed(),
              d.getTechnicianId(),
              d.getSafetyChecklistCompleted());
    }

    return new ReservationDTO.Response(
        r.getId(),
        r.getReservationNumber(),
        r.getPatient().getId(),
        r.getPatient().getFirstName() + " " + r.getPatient().getLastName(),
        r.getPatient().getPhone(),
        r.getCategory().getId(),
        r.getCategory().getName(),
        r.getCategory().getCode(),
        r.getDepartmentId(),
        r.getPractitionerId(),
        r.getPrescription() != null ? r.getPrescription().getId() : null,
        r.getRoom() != null ? r.getRoom().getId() : null,
        r.getRoom() != null ? r.getRoom().getName() : null,
        r.getReservationDate(),
        r.getStartTime(),
        r.getEndTime(),
        r.getStatus().getId(),
        r.getStatus().getCode(),
        r.getStatus().getName(),
        r.getStatus().getBadgeColorHex(),
        r.getIsPaid(),
        r.getBillingInvoiceId(),
        r.getReasonForVisit(),
        r.getNotes(),
        r.getReceptionistStaffId(),
        diagDetail,
        r.getCreatedAt(),
        r.getUpdatedAt());
  }
}
