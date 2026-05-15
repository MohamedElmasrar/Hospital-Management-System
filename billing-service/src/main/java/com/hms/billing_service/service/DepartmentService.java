package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.department.CreateDepartmentRequestDTO;
import com.hms.billing_service.dto.department.DepartmentResponseDTO;
import com.hms.billing_service.dto.department.UpdateDepartmentRequestDTO;
import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.exception.DepartmentNotFoundException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.mapper.DepartmentMapper;
import com.hms.billing_service.model.Department;
import com.hms.billing_service.repository.DepartmentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final DepartmentMapper departmentMapper;

  @Transactional
  public DepartmentResponseDTO create(CreateDepartmentRequestDTO request) {
    if (departmentRepository.existsByName(request.name())) {
      throw new ResourceAlreadyExistsException(
          "Department already exists with name: " + request.name());
    }
    Department department = departmentMapper.createDepartmentRequestDTOToDepartment(request);
    Department saved = departmentRepository.save(department);
    return departmentMapper.departmentToDepartmentResponseDTO(saved);
  }

  @Transactional
  public DepartmentResponseDTO update(UUID id, UpdateDepartmentRequestDTO request) {
    Department department =
        departmentRepository
            .findById(id)
            .orElseThrow(
                () -> new DepartmentNotFoundException("Department not found with ID: " + id));

    if (request.name() != null
        && !request.name().equals(department.getName())
        && departmentRepository.existsByName(request.name())) {
      throw new ResourceAlreadyExistsException(
          "Department already exists with name: " + request.name());
    }

    departmentMapper.updateDepartmentFromDTO(request, department);
    Department updated = departmentRepository.save(department);
    return departmentMapper.departmentToDepartmentResponseDTO(updated);
  }

  @Transactional(readOnly = true)
  public DepartmentResponseDTO getById(UUID id) {
    Department department =
        departmentRepository
            .findById(id)
            .orElseThrow(
                () -> new DepartmentNotFoundException("Department not found with ID: " + id));
    return departmentMapper.departmentToDepartmentResponseDTO(department);
  }

  @Transactional(readOnly = true)
  public PageResponse<DepartmentResponseDTO> getAll(Pageable pageable) {
    return PageResponse.from(
        departmentRepository
            .findAll(pageable)
            .map(departmentMapper::departmentToDepartmentResponseDTO));
  }

  @Transactional(readOnly = true)
  public PageResponse<DepartmentResponseDTO> getBySection(
      BillingSectionType section, Pageable pageable) {
    return PageResponse.from(
        departmentRepository
            .findBySection(section, pageable)
            .map(departmentMapper::departmentToDepartmentResponseDTO));
  }

  @Transactional
  public void delete(UUID id) {
    if (!departmentRepository.existsById(id)) {
      throw new DepartmentNotFoundException("Department not found with ID: " + id);
    }
    departmentRepository.deleteById(id);
  }
}
