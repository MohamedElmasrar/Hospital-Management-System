package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.exception.ResourceNotFoundException;
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
  private final SecureRandom random = new SecureRandom();

  @Transactional
  public PatientDTO.Response createPatient(PatientDTO.CreateRequest req) {
    if (req.nationalId() != null
        && !req.nationalId().isBlank()
        && patientRepository.existsByNationalId(req.nationalId())) {
      throw new ResourceAlreadyExistsException(
          "Patient with National ID " + req.nationalId() + " already exists");
    }

    Patient patient = new Patient();
    patient.setMrn(generateUniqueMrn());
    patient.setUserId(req.userId());
    patient.setNationalId(
        req.nationalId() != null && !req.nationalId().isBlank() ? req.nationalId() : null);
    patient.setFirstName(req.firstName());
    patient.setLastName(req.lastName());
    patient.setDateOfBirth(req.dateOfBirth());
    patient.setGender(req.gender());
    patient.setBloodGroup(req.bloodGroup() != null ? req.bloodGroup() : "UNKNOWN");
    patient.setPhone(req.phone());
    patient.setEmergencyContactPhone(req.emergencyContactPhone());
    patient.setEmergencyContactName(req.emergencyContactName());
    patient.setAllergies(req.allergies());
    patient.setChronicConditions(req.chronicConditions());
    patient.setIsActive(Boolean.TRUE);

    return mapToDTO(patientRepository.save(patient));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientById(UUID id) {
    return patientRepository
        .findById(id)
        .map(this::mapToDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientByMrn(String mrn) {
    return patientRepository
        .findByMrn(mrn)
        .map(this::mapToDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with MRN: " + mrn));
  }

  @Transactional(readOnly = true)
  public PatientDTO.Response getPatientByUserId(UUID userId) {
    return patientRepository
        .findByUserId(userId)
        .map(this::mapToDTO)
        .orElseThrow(
            () -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
  }

  @Transactional(readOnly = true)
  public PageResponse<PatientDTO.Response> searchPatients(String query, Pageable pageable) {
    return PageResponse.from(patientRepository.searchPatients(query, pageable).map(this::mapToDTO));
  }

  @Transactional
  public PatientDTO.Response updatePatient(UUID id, PatientDTO.UpdateRequest req) {
    Patient patient =
        patientRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));

    if (req.firstName() != null) patient.setFirstName(req.firstName());
    if (req.lastName() != null) patient.setLastName(req.lastName());
    if (req.dateOfBirth() != null) patient.setDateOfBirth(req.dateOfBirth());
    if (req.gender() != null) patient.setGender(req.gender());
    if (req.bloodGroup() != null) patient.setBloodGroup(req.bloodGroup());
    if (req.phone() != null) patient.setPhone(req.phone());
    if (req.emergencyContactPhone() != null)
      patient.setEmergencyContactPhone(req.emergencyContactPhone());
    if (req.emergencyContactName() != null)
      patient.setEmergencyContactName(req.emergencyContactName());
    if (req.allergies() != null) patient.setAllergies(req.allergies());
    if (req.chronicConditions() != null) patient.setChronicConditions(req.chronicConditions());
    if (req.isActive() != null) patient.setIsActive(req.isActive());

    return mapToDTO(patientRepository.save(patient));
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

  public PatientDTO.Response mapToDTO(Patient p) {
    return new PatientDTO.Response(
        p.getId(),
        p.getMrn(),
        p.getUserId(),
        p.getNationalId(),
        p.getFirstName(),
        p.getLastName(),
        p.getDateOfBirth(),
        p.getGender(),
        p.getBloodGroup(),
        p.getPhone(),
        p.getEmergencyContactPhone(),
        p.getEmergencyContactName(),
        p.getAllergies(),
        p.getChronicConditions(),
        p.getIsActive(),
        p.getDateCreated(),
        p.getDateModified());
  }
}
