package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.admission.AdmissionDTO;
import com.hms.reception_service.dto.admission.EmergencyAdmissionDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.mapper.AdmissionMapper;
import com.hms.reception_service.messaging.AnalyticsEventPublisher;
import com.hms.reception_service.model.Admission;
import com.hms.reception_service.model.Bed;
import com.hms.reception_service.model.EmergencyAdmission;
import com.hms.reception_service.repository.*;
import com.hms.reception_service.specification.AdmissionSpecifications;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdmissionService {
  private final BedRepository bedRepository;
  private final AdmissionRepository admissionRepository;
  private final EmergencyAdmissionRepository emergencyAdmissionRepository;
  private final PatientRepository patientRepository;
  private final AnalyticsEventPublisher analyticsEventPublisher;
  private final AdmissionMapper admissionMapper;

  @Transactional
  public AdmissionDTO.Response createBed(AdmissionDTO.CreateBedRequest request) {
    Bed bed = new Bed();
    bed.setBedCode(request.bedCode());
    bed.setWard(request.ward());
    return admissionMapper.toBedResponse(bedRepository.save(bed));
  }

  @Transactional(readOnly = true)
  public List<AdmissionDTO.Response> availableBeds() {
    return bedRepository.findByOccupiedFalse().stream()
        .map(admissionMapper::toBedResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<AdmissionDTO.Response> searchAdmissions(
      String query, boolean activeOnly, Pageable pageable) {
    return PageResponse.from(
        admissionRepository
            .findAll(AdmissionSpecifications.search(query, activeOnly), pageable)
            .map(admissionMapper::toResponse));
  }

  @Transactional
  public AdmissionDTO.Response admit(AdmissionDTO.AdmitRequest request) {
    var patient =
        patientRepository
            .findById(request.patientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    Bed bed =
        bedRepository
            .findById(request.bedId())
            .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
    if (bed.isOccupied()
        || admissionRepository.findByPatientIdAndDischargedAtIsNull(patient.getId()).isPresent()) {
      throw new ResourceAlreadyExistsException("Patient or bed already has an active admission");
    }
    bed.setOccupied(true);
    bed.setPatientId(patient.getId());
    Admission admission = new Admission();
    admission.setPatient(patient);
    admission.setBed(bed);
    admission.setReason(request.reason());
    admission.setAdmittedAt(LocalDateTime.now());
    bedRepository.save(bed);
    Admission saved = admissionRepository.save(admission);
    analyticsEventPublisher.publish("PATIENT_ADMITTED", "ADMISSION", saved.getId());
    return admissionMapper.toResponse(saved);
  }

  @Transactional
  public AdmissionDTO.Response discharge(UUID admissionId) {
    Admission admission =
        admissionRepository
            .findById(admissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));
    if (admission.getDischargedAt() != null) {
      throw new InvalidOperationException("Admission is already discharged");
    }
    admission.setDischargedAt(LocalDateTime.now());
    Bed bed = admission.getBed();
    bed.setOccupied(false);
    bed.setPatientId(null);
    bedRepository.save(bed);
    Admission saved = admissionRepository.save(admission);
    analyticsEventPublisher.publish("PATIENT_DISCHARGED", "ADMISSION", saved.getId());
    return admissionMapper.toResponse(saved);
  }

  @Transactional
  public EmergencyAdmissionDTO.Response emergency(EmergencyAdmissionDTO.CreateRequest request) {
    EmergencyAdmission admission = new EmergencyAdmission();
    admission.setDisplayName(request.displayName());
    admission.setPhone(request.phone());
    admission.setChiefComplaint(request.chiefComplaint());
    if (request.patientId() != null) {
      admission.setPatient(
          patientRepository
              .findById(request.patientId())
              .orElseThrow(() -> new ResourceNotFoundException("Patient not found")));
    }
    EmergencyAdmission saved = emergencyAdmissionRepository.save(admission);
    analyticsEventPublisher.publish(
        "EMERGENCY_ADMISSION_CREATED", "EMERGENCY_ADMISSION", saved.getId());
    return admissionMapper.toEmergencyResponse(saved);
  }
}
