package com.hms.reception_service.service;

import com.hms.reception_service.dto.prescription.DiagnosticPrescriptionDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.mapper.DiagnosticPrescriptionMapper;
import com.hms.reception_service.model.DiagnosticModality;
import com.hms.reception_service.model.DiagnosticPrescription;
import com.hms.reception_service.model.Patient;
import com.hms.reception_service.repository.DiagnosticModalityRepository;
import com.hms.reception_service.repository.DiagnosticPrescriptionRepository;
import com.hms.reception_service.repository.PatientRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosticPrescriptionService {

  private final DiagnosticPrescriptionRepository prescriptionRepository;
  private final PatientRepository patientRepository;
  private final DiagnosticModalityRepository modalityRepository;
  private final HrGrpcClient hrGrpcClient;
  private final DiagnosticPrescriptionMapper prescriptionMapper;
  private final SecureRandom random = new SecureRandom();

  @Transactional
  public DiagnosticPrescriptionDTO.Response createPrescription(
      DiagnosticPrescriptionDTO.CreateRequest req, UUID doctorId) {
    if (!hrGrpcClient.validatePhysician(doctorId)) {
      throw new InvalidOperationException("Physician not valid in HR: " + doctorId);
    }
    Patient patient =
        patientRepository
            .findById(req.patientId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Patient not found: " + req.patientId()));

    DiagnosticModality modality =
        modalityRepository
            .findById(req.modalityId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Modality not found: " + req.modalityId()));

    DiagnosticPrescription rx = prescriptionMapper.toEntity(req);
    rx.setPrescriptionCode(generateUniquePrescriptionCode());
    rx.setPatient(patient);
    rx.setReferringPractitionerId(doctorId);
    rx.setModality(modality);

    return prescriptionMapper.toResponse(prescriptionRepository.save(rx));
  }

  @Transactional(readOnly = true)
  public List<DiagnosticPrescriptionDTO.Response> getPendingPrescriptionsByPatient(UUID patientId) {
    return prescriptionMapper.toResponseList(
        prescriptionRepository.findByPatientIdAndIsFulfilledFalse(patientId));
  }

  @Transactional(readOnly = true)
  public DiagnosticPrescriptionDTO.Response getPrescriptionById(UUID id) {
    return prescriptionRepository
        .findById(id)
        .map(prescriptionMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
  }

  @Transactional
  public void markPrescriptionFulfilled(UUID prescriptionId) {
    DiagnosticPrescription rx =
        prescriptionRepository
            .findById(prescriptionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));
    rx.setIsFulfilled(Boolean.TRUE);
    prescriptionRepository.save(rx);
  }

  private String generateUniquePrescriptionCode() {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    int rand = 1000 + random.nextInt(9000);
    return "RX-" + datePart + "-" + rand;
  }
}
