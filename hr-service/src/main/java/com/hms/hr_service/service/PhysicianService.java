package com.hms.hr_service.service;

import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.Physician.AssignPhysicianSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.CreateMedicalSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.CreatePhysicianRequestDTO;
import com.hms.hr_service.dto.Physician.MedicalSpecialtyResponseDTO;
import com.hms.hr_service.dto.Physician.PhysicianResponseDTO;
import com.hms.hr_service.dto.Physician.PhysicianSpecialtyResponseDTO;
import com.hms.hr_service.dto.Physician.UpdatePhysicianRequestDTO;
import com.hms.hr_service.exception.EmployeeNotFoundException;
import com.hms.hr_service.exception.MedicalSpecialtyNotFoundException;
import com.hms.hr_service.exception.PhysicianNotFoundException;
import com.hms.hr_service.exception.ResourceAlreadyExistsException;
import com.hms.hr_service.mapper.MedicalSpecialtyMapper;
import com.hms.hr_service.mapper.PhysicianMapper;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.MedicalSpecialty;
import com.hms.hr_service.model.Physician;
import com.hms.hr_service.model.PhysicianSpecialty;
import com.hms.hr_service.repository.EmployeeRepository;
import com.hms.hr_service.repository.MedicalSpecialtyRepository;
import com.hms.hr_service.repository.PhysicianRepository;
import com.hms.hr_service.repository.PhysicianSpecialtyRepository;
import com.hms.hr_service.specification.PhysicianSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhysicianService {

  private final PhysicianRepository physicianRepository;
  private final EmployeeRepository employeeRepository;
  private final MedicalSpecialtyRepository medicalSpecialtyRepository;
  private final PhysicianSpecialtyRepository physicianSpecialtyRepository;
  private final PhysicianMapper physicianMapper;
  private final MedicalSpecialtyMapper medicalSpecialtyMapper;

  @Transactional
  public PhysicianResponseDTO create(CreatePhysicianRequestDTO request) {
    Employee employee =
        employeeRepository
            .findById(request.employeeId())
            .orElseThrow(EmployeeNotFoundException::new);

    if (physicianRepository.existsByEmployeeId(request.employeeId())) {
      throw new ResourceAlreadyExistsException("Employee is already registered as a physician");
    }

    if (physicianRepository.existsByLicenseNumber(request.licenseNumber())) {
      throw new ResourceAlreadyExistsException(
          "Physician with license " + request.licenseNumber() + " already exists");
    }

    Physician physician = physicianMapper.toPhysician(request, employee);
    Physician savedPhysician = physicianRepository.save(physician);

    List<PhysicianSpecialtyResponseDTO> specialties = new ArrayList<>();
    if (request.specialtyIds() != null && !request.specialtyIds().isEmpty()) {
      for (UUID specialtyId : request.specialtyIds()) {
        MedicalSpecialty specialty =
            medicalSpecialtyRepository
                .findById(specialtyId)
                .orElseThrow(MedicalSpecialtyNotFoundException::new);

        boolean isPrimary =
            request.primarySpecialtyId() != null
                && specialtyId.equals(request.primarySpecialtyId());
        PhysicianSpecialty ps =
            physicianMapper.toPhysicianSpecialty(savedPhysician, specialty, isPrimary);

        specialties.add(
            physicianMapper.toPhysicianSpecialtyResponseDTO(physicianSpecialtyRepository.save(ps)));
      }
    }

    return physicianMapper.toPhysicianResponseDTO(savedPhysician, specialties);
  }

  @Transactional
  public PhysicianResponseDTO update(UUID id, UpdatePhysicianRequestDTO request) {
    Physician physician =
        physicianRepository.findById(id).orElseThrow(PhysicianNotFoundException::new);

    if (!physician.getLicenseNumber().equals(request.licenseNumber())
        && physicianRepository.existsByLicenseNumber(request.licenseNumber())) {
      throw new ResourceAlreadyExistsException(
          "Physician with license " + request.licenseNumber() + " already exists");
    }

    physicianMapper.updatePhysicianFromDTO(request, physician);
    Physician updated = physicianRepository.save(physician);

    return toResponseDTO(updated);
  }

  @Transactional(readOnly = true)
  public PhysicianResponseDTO getById(UUID id) {
    Physician physician =
        physicianRepository.findById(id).orElseThrow(PhysicianNotFoundException::new);
    return toResponseDTO(physician);
  }

  @Transactional(readOnly = true)
  public PhysicianResponseDTO getByUserId(UUID userId) {
    Employee employee =
        employeeRepository.findByUserId(userId).orElseThrow(EmployeeNotFoundException::new);
    Physician physician =
        physicianRepository
            .findByEmployeeId(employee.getId())
            .orElseThrow(PhysicianNotFoundException::new);
    return toResponseDTO(physician);
  }

  @Transactional(readOnly = true)
  public PageResponse<PhysicianResponseDTO> search(String search, Pageable pageable) {
    Specification<Physician> spec = PhysicianSpecification.hasSearch(search);
    Page<Physician> page = physicianRepository.findAll(spec, pageable);
    return PageResponse.from(page.map(this::toResponseDTO));
  }

  @Transactional
  public PhysicianResponseDTO assignSpecialty(
      UUID physicianId, AssignPhysicianSpecialtyRequestDTO request) {
    Physician physician =
        physicianRepository.findById(physicianId).orElseThrow(PhysicianNotFoundException::new);
    MedicalSpecialty specialty =
        medicalSpecialtyRepository
            .findById(request.medicalSpecialtyId())
            .orElseThrow(MedicalSpecialtyNotFoundException::new);

    if (request.isPrimary()) {
      physicianSpecialtyRepository
          .findByPhysicianId(physicianId)
          .forEach(
              ps -> {
                if (ps.isPrimary()) {
                  ps.setPrimary(false);
                  physicianSpecialtyRepository.save(ps);
                }
              });
    }

    PhysicianSpecialty ps =
        physicianSpecialtyRepository
            .findByPhysicianIdAndMedicalSpecialtyId(physicianId, request.medicalSpecialtyId())
            .orElseGet(
                () ->
                    physicianMapper.toPhysicianSpecialty(
                        physician, specialty, request.isPrimary()));

    ps.setPrimary(request.isPrimary());
    physicianSpecialtyRepository.save(ps);

    return toResponseDTO(physician);
  }

  @Transactional
  public void removeSpecialty(UUID physicianId, UUID specialtyId) {
    if (!physicianRepository.existsById(physicianId)) {
      throw new PhysicianNotFoundException();
    }
    if (!medicalSpecialtyRepository.existsById(specialtyId)) {
      throw new MedicalSpecialtyNotFoundException();
    }
    physicianSpecialtyRepository.deleteByPhysicianIdAndMedicalSpecialtyId(physicianId, specialtyId);
  }

  // --- Specialty catalog operations ---

  @Transactional
  public MedicalSpecialtyResponseDTO createSpecialty(CreateMedicalSpecialtyRequestDTO request) {
    if (medicalSpecialtyRepository.existsByName(request.name())) {
      throw new ResourceAlreadyExistsException(
          "Medical specialty '" + request.name() + "' already exists");
    }
    MedicalSpecialty specialty =
        medicalSpecialtyMapper.createMedicalSpecialtyRequestDTOToMedicalSpecialty(request);
    return medicalSpecialtyMapper.medicalSpecialtyToMedicalSpecialtyResponseDTO(
        medicalSpecialtyRepository.save(specialty));
  }

  @Transactional(readOnly = true)
  public List<MedicalSpecialtyResponseDTO> getAllSpecialties() {
    return medicalSpecialtyMapper.toMedicalSpecialtyResponseDTOList(
        medicalSpecialtyRepository.findAll());
  }

  @Transactional(readOnly = true)
  public MedicalSpecialtyResponseDTO getSpecialtyById(UUID id) {
    return medicalSpecialtyMapper.medicalSpecialtyToMedicalSpecialtyResponseDTO(
        medicalSpecialtyRepository
            .findById(id)
            .orElseThrow(MedicalSpecialtyNotFoundException::new));
  }

  private PhysicianResponseDTO toResponseDTO(Physician physician) {
    List<PhysicianSpecialty> specialties =
        physicianSpecialtyRepository.findByPhysicianId(physician.getId());
    return physicianMapper.toPhysicianResponseDTO(
        physician, physicianMapper.toPhysicianSpecialtyResponseDTOList(specialties));
  }
}
