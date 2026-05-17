package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.mapper.PatientMapper;
import com.hms.reception_service.model.Patient;
import com.hms.reception_service.repository.PatientRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;
  private final PatientMapper patientMapper;
  private final SecureRandom random = new SecureRandom();

  @Transactional
  public PatientDTO.Response createPatient(PatientDTO.CreateRequest req) {
    if (req.nationalId() != null
        && !req.nationalId().isBlank()
        && patientRepository.existsByNationalId(req.nationalId())) {
      throw new ResourceAlreadyExistsException(
          "Patient with National ID " + req.nationalId() + " already exists");
    }

    Patient patient = patientMapper.toEntity(req);
    patient.setMrn(generateUniqueMrn());

    return patientMapper.toResponse(patientRepository.save(patient));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientById(UUID id) {
    return patientRepository
        .findById(id)
        .map(patientMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientByMrn(String mrn) {
    return patientRepository
        .findByMrn(mrn)
        .map(patientMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with MRN: " + mrn));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientByUserId(UUID userId) {
    return patientRepository
        .findByUserId(userId)
        .map(patientMapper::toResponse)
        .orElseThrow(
            () -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
  }

  @Transactional(readOnly = true)
  public PageResponse<PatientDTO.Response> searchPatients(String query, Pageable pageable) {
    return PageResponse.from(
        patientRepository.searchPatients(query, pageable).map(patientMapper::toResponse));
  }

  @Transactional
  public PatientDTO.Response updatePatient(UUID id, PatientDTO.UpdateRequest req) {
    Patient patient =
        patientRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));

    patientMapper.updatePatientFromDTO(req, patient);

    return patientMapper.toResponse(patientRepository.save(patient));
  }

  private String generateUniqueMrn() {
    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String mrn;
    do {
      int randomNum = 1000 + random.nextInt(9000);
      mrn = "MRN-" + datePart + "-" + randomNum;
    } while (patientRepository.existsByMrn(mrn));
    return mrn;
  }
}
