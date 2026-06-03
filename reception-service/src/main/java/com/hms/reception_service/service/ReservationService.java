package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.mapper.ReservationMapper;
import com.hms.reception_service.messaging.AnalyticsEventPublisher;
import com.hms.reception_service.model.DiagnosticModality;
import com.hms.reception_service.model.DiagnosticPrescription;
import com.hms.reception_service.model.DiagnosticReservationDetail;
import com.hms.reception_service.model.ExaminationRoom;
import com.hms.reception_service.model.Patient;
import com.hms.reception_service.model.Reservation;
import com.hms.reception_service.model.ReservationCategory;
import com.hms.reception_service.model.ReservationStatus;
import com.hms.reception_service.repository.DiagnosticModalityRepository;
import com.hms.reception_service.repository.DiagnosticPrescriptionRepository;
import com.hms.reception_service.repository.ExaminationRoomRepository;
import com.hms.reception_service.repository.PatientRepository;
import com.hms.reception_service.repository.ReservationCategoryRepository;
import com.hms.reception_service.repository.ReservationRepository;
import com.hms.reception_service.repository.ReservationStatusRepository;
import com.hms.reception_service.specification.ReservationSpecifications;
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
  private final ReservationMapper reservationMapper;
  private final AnalyticsEventPublisher analyticsEventPublisher;
  private final BillingPaymentVerifier billingPaymentVerifier;
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
        reservationRepository.findAll(
            ReservationSpecifications.conflictingPractitioner(
                req.practitionerId(), req.reservationDate(), req.startTime(), req.endTime()));
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
      if (!reservationRepository
          .findAll(
              ReservationSpecifications.conflictingRoom(
                  req.roomId(), req.reservationDate(), req.startTime(), req.endTime()))
          .isEmpty()) {
        throw new InvalidOperationException(
            "Room is already booked during the requested time slot");
      }
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

    Reservation saved = reservationRepository.save(res);
    analyticsEventPublisher.publish(
        "CONSULTATION_RESERVED",
        "RESERVATION",
        saved.getId(),
        "department_id",
        req.departmentId().toString());
    return reservationMapper.toResponse(saved);
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
        reservationRepository.findAll(
            ReservationSpecifications.conflictingRoom(
                req.roomId(), req.reservationDate(), req.startTime(), req.endTime()));
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

    Reservation saved = reservationRepository.save(res);
    analyticsEventPublisher.publish(
        "DIAGNOSTIC_RESERVED",
        "RESERVATION",
        saved.getId(),
        "department_id",
        req.departmentId().toString());
    return reservationMapper.toResponse(saved);
  }

  @Transactional
  public ReservationDTO.Response markAsPaid(UUID reservationId, UUID invoiceId) {
    Reservation res =
        reservationRepository
            .findById(reservationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Reservation not found: " + reservationId));

    if (!"PENDING_PAYMENT".equals(res.getStatus().getCode())
        && !"PAID".equals(res.getStatus().getCode())) {
      throw new InvalidOperationException("Only reservations awaiting payment can be approved");
    }
    billingPaymentVerifier.requirePaidInvoice(invoiceId, res.getPatient().getId(), reservationId);
    ReservationStatus paidStatus =
        statusRepository
            .findByCode("PAID")
            .orElseThrow(() -> new ResourceNotFoundException("Status PAID not found"));

    boolean becamePaid = !Boolean.TRUE.equals(res.getIsPaid());
    res.setIsPaid(Boolean.TRUE);
    res.setStatus(paidStatus);
    res.setBillingInvoiceId(invoiceId);

    Reservation saved = reservationRepository.save(res);
    if (becamePaid) {
      analyticsEventPublisher.publish("RESERVATION_PAID", "RESERVATION", saved.getId());
    }
    return reservationMapper.toResponse(saved);
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

    String oldStatus = res.getStatus().getCode();
    res.setStatus(newStatus);
    if (req.notes() != null && !req.notes().isBlank()) {
      res.setNotes((res.getNotes() != null ? res.getNotes() + "\n" : "") + req.notes());
    }

    boolean prescriptionFulfilled =
        "COMPLETED".equals(newStatus.getCode())
            && res.getPrescription() != null
            && !Boolean.TRUE.equals(res.getPrescription().getIsFulfilled());
    if (prescriptionFulfilled) {
      res.getPrescription().setIsFulfilled(Boolean.TRUE);
    }

    Reservation saved = reservationRepository.save(res);
    if (!oldStatus.equals(newStatus.getCode())
        && ("COMPLETED".equals(newStatus.getCode()) || "CANCELLED".equals(newStatus.getCode()))) {
      analyticsEventPublisher.publish(
          "RESERVATION_" + newStatus.getCode(), "RESERVATION", saved.getId());
    }
    if (prescriptionFulfilled) {
      analyticsEventPublisher.publish(
          "DIAGNOSTIC_PRESCRIPTION_FULFILLED",
          "DIAGNOSTIC_PRESCRIPTION",
          saved.getPrescription().getId());
    }
    return reservationMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public ReservationDTO.Response getReservationById(UUID id) {
    return reservationRepository
        .findById(id)
        .map(reservationMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReservationDTO.Response> getReservationsByPatient(
      UUID patientId, Pageable pageable) {
    return PageResponse.from(
        reservationRepository
            .findByPatientId(patientId, pageable)
            .map(reservationMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReservationDTO.Response> searchReservations(
      String query, LocalDate date, Pageable pageable) {
    return PageResponse.from(
        reservationRepository
            .findAll(ReservationSpecifications.search(query, date), pageable)
            .map(reservationMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReservationDTO.Response> searchUnpaidConsultations(
      String query, Pageable pageable) {
    return PageResponse.from(
        reservationRepository
            .findAll(ReservationSpecifications.awaitingConsultationPayment(query), pageable)
            .map(reservationMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReservationDTO.Response> searchDoctorConsultations(
      UUID practitionerId, String query, LocalDate date, Pageable pageable) {
    return PageResponse.from(
        reservationRepository
            .findAll(
                ReservationSpecifications.doctorConsultations(practitionerId, query, date),
                pageable)
            .map(reservationMapper::toResponse));
  }

  private String generateReservationNumber(String prefix) {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    int rand = 1000 + random.nextInt(9000);
    return prefix + "-" + datePart + "-" + rand;
  }
}
