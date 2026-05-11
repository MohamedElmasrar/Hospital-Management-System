package com.hms.hr_service.service;

import com.hms.hr_service.dto.Department.CreateDepartmentRequestDTO;
import com.hms.hr_service.dto.Department.DepartmentResponseDTO;
import com.hms.hr_service.dto.Department.UpdateDepartmentRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.exception.DepartmentNotFoundException;
import com.hms.hr_service.exception.MedicalOrganizationNotFoundException;
import com.hms.hr_service.mapper.DepartmentMapper;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.DepartmentRepository;
import com.hms.hr_service.repository.MedicalOrganizationRepository;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final MedicalOrganizationRepository medicalOrganizationRepository;
  private final DepartmentMapper departmentMapper;

  @Transactional
  public DepartmentResponseDTO create(CreateDepartmentRequestDTO request) {
    return create(request, null);
  }

  @Transactional
  public DepartmentResponseDTO create(
      CreateDepartmentRequestDTO request, AuthenticatedPrincipal principal) {
    MedicalOrganization organization =
        (principal != null && UserRole.ADMIN.equals(principal.role()) && principal.userId() != null)
            ? medicalOrganizationRepository
                .findByIdAndAdminId(request.medicalOrganizationId(), principal.userId())
                .orElseThrow(MedicalOrganizationNotFoundException::new)
            : medicalOrganizationRepository
                .findById(request.medicalOrganizationId())
                .orElseThrow(MedicalOrganizationNotFoundException::new);

    Department department =
        departmentMapper.createDepartmentRequestDTOToDepartment(request, organization);

    if (request.parentDepartmentId() != null) {
      Department parent =
          departmentRepository
              .findByIdAndMedicalOrganizationId(request.parentDepartmentId(), organization.getId())
              .orElseThrow(() -> new DepartmentNotFoundException("Parent department not found"));
      department.setParentDepartment(parent);
    }

    return departmentMapper.departmentToDepartmentResponseDTO(
        departmentRepository.save(department));
  }

  @Transactional
  public DepartmentResponseDTO update(UUID id, UpdateDepartmentRequestDTO request) {
    return update(id, request, null);
  }

  @Transactional
  public DepartmentResponseDTO update(
      UUID id, UpdateDepartmentRequestDTO request, AuthenticatedPrincipal principal) {
    Department department =
        departmentRepository.findById(id).orElseThrow(DepartmentNotFoundException::new);

    departmentMapper.updateDepartmentFromDTO(request, department);

    if (request.parentDepartmentId() != null) {
      Department parent =
          departmentRepository
              .findByIdAndMedicalOrganizationId(
                  request.parentDepartmentId(), department.getMedicalOrganization().getId())
              .orElseThrow(() -> new DepartmentNotFoundException("Parent department not found"));
      department.setParentDepartment(parent);
    } else {
      department.setParentDepartment(null);
    }

    return departmentMapper.departmentToDepartmentResponseDTO(
        departmentRepository.save(department));
  }

  @Transactional(readOnly = true)
  public DepartmentResponseDTO getById(UUID id) {
    return departmentMapper.departmentToDepartmentResponseDTO(
        departmentRepository.findById(id).orElseThrow(DepartmentNotFoundException::new));
  }

  @Transactional(readOnly = true)
  public PageResponse<DepartmentResponseDTO> getByOrganization(
      UUID organizationId, Pageable pageable) {
    if (!medicalOrganizationRepository.existsById(organizationId)) {
      throw new MedicalOrganizationNotFoundException();
    }
    Page<Department> page =
        departmentRepository.findByMedicalOrganizationId(organizationId, pageable);
    return PageResponse.from(page.map(departmentMapper::departmentToDepartmentResponseDTO));
  }

  @Transactional(readOnly = true)
  public List<DepartmentResponseDTO> listByOrganization(UUID organizationId) {
    if (!medicalOrganizationRepository.existsById(organizationId)) {
      throw new MedicalOrganizationNotFoundException();
    }
    return departmentMapper.toDepartmentResponseDTOList(
        departmentRepository.findByMedicalOrganizationId(organizationId));
  }

  @Transactional
  public void delete(UUID id) {
    delete(id, null);
  }

  @Transactional
  public void delete(UUID id, AuthenticatedPrincipal principal) {
    Department department =
        departmentRepository.findById(id).orElseThrow(DepartmentNotFoundException::new);
    departmentRepository.delete(department);
  }
}
