package com.hms.hr_service.service;

import com.hms.hr_service.dto.Employee.AssignEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.ContactPointResponseDTO;
import com.hms.hr_service.dto.Employee.CreateContactPointRequestDTO;
import com.hms.hr_service.dto.Employee.CreateEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.EmployeeAssignmentResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeDetailResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeResponseDTO;
import com.hms.hr_service.dto.Employee.PostalAddressDTO;
import com.hms.hr_service.dto.Employee.UpdateEmployeeRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.exception.DepartmentNotFoundException;
import com.hms.hr_service.exception.EmployeeNotFoundException;
import com.hms.hr_service.exception.ResourceAlreadyExistsException;
import com.hms.hr_service.mapper.EmployeeMapper;
import com.hms.hr_service.model.ContactPoint;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.model.PostalAddress;
import com.hms.hr_service.repository.ContactPointRepository;
import com.hms.hr_service.repository.DepartmentRepository;
import com.hms.hr_service.repository.EmployeeAssignmentRepository;
import com.hms.hr_service.repository.EmployeeRepository;
import com.hms.hr_service.repository.PostalAddressRepository;
import com.hms.hr_service.specification.EmployeeSpecification;
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
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final ContactPointRepository contactPointRepository;
  private final PostalAddressRepository postalAddressRepository;
  private final EmployeeAssignmentRepository employeeAssignmentRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeMapper employeeMapper;

  @Transactional
  public EmployeeResponseDTO create(CreateEmployeeRequestDTO request) {
    if (employeeRepository.existsByIdentifier(request.identifier())) {
      throw new ResourceAlreadyExistsException(
          "Employee with identifier " + request.identifier() + " already exists");
    }

    Employee employee = employeeMapper.createEmployeeRequestDTOToEmployee(request);
    Employee saved = employeeRepository.save(employee);

    if (request.email() != null || request.telephone() != null) {
      contactPointRepository.save(
          employeeMapper.createEmployeeRequestDTOToContactPoint(request, saved));
    }

    if (request.streetAddress() != null) {
      postalAddressRepository.save(
          employeeMapper.createEmployeeRequestDTOToPostalAddress(request, saved));
    }

    return employeeMapper.employeeToEmployeeResponseDTO(saved);
  }

  @Transactional
  public EmployeeResponseDTO update(UUID id, UpdateEmployeeRequestDTO request) {
    Employee employee = employeeRepository.findById(id).orElseThrow(EmployeeNotFoundException::new);
    employeeMapper.updateEmployeeFromDTO(request, employee);
    return employeeMapper.employeeToEmployeeResponseDTO(employeeRepository.save(employee));
  }

  @Transactional(readOnly = true)
  public EmployeeDetailResponseDTO getById(UUID id) {
    Employee employee = employeeRepository.findById(id).orElseThrow(EmployeeNotFoundException::new);
    List<ContactPoint> contactPoints = contactPointRepository.findByEmployeeId(id);
    PostalAddress address = postalAddressRepository.findByEmployeeId(id).orElse(null);
    List<EmployeeAssignment> assignments = employeeAssignmentRepository.findByEmployeeId(id);

    return employeeMapper.toEmployeeDetailResponseDTO(
        employee, contactPoints, address, assignments);
  }

  @Transactional(readOnly = true)
  public EmployeeDetailResponseDTO getByUserId(UUID userId) {
    Employee employee =
        employeeRepository.findByUserId(userId).orElseThrow(EmployeeNotFoundException::new);
    return getById(employee.getId());
  }

  @Transactional(readOnly = true)
  public PageResponse<EmployeeResponseDTO> search(
      String search, EmploymentStatus status, Pageable pageable) {
    Specification<Employee> spec =
        Specification.where(EmployeeSpecification.hasSearch(search))
            .and(EmployeeSpecification.hasEmploymentStatus(status));

    Page<Employee> page = employeeRepository.findAll(spec, pageable);
    return PageResponse.from(page.map(employeeMapper::employeeToEmployeeResponseDTO));
  }

  @Transactional
  public void delete(UUID id) {
    Employee employee = employeeRepository.findById(id).orElseThrow(EmployeeNotFoundException::new);
    employeeRepository.delete(employee);
  }

  @Transactional
  public EmployeeAssignmentResponseDTO assignToDepartment(
      UUID employeeId, AssignEmployeeRequestDTO request) {
    Employee employee =
        employeeRepository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    Department department =
        departmentRepository
            .findById(request.departmentId())
            .orElseThrow(DepartmentNotFoundException::new);

    EmployeeAssignment assignment =
        employeeMapper.toEmployeeAssignment(request, employee, department);

    return employeeMapper.employeeAssignmentToEmployeeAssignmentResponseDTO(
        employeeAssignmentRepository.save(assignment));
  }

  @Transactional(readOnly = true)
  public List<EmployeeAssignmentResponseDTO> getAssignments(UUID employeeId) {
    if (!employeeRepository.existsById(employeeId)) {
      throw new EmployeeNotFoundException();
    }
    return employeeAssignmentRepository.findByEmployeeId(employeeId).stream()
        .map(employeeMapper::employeeAssignmentToEmployeeAssignmentResponseDTO)
        .toList();
  }

  @Transactional
  public ContactPointResponseDTO addContactPoint(
      UUID employeeId, CreateContactPointRequestDTO request) {
    Employee employee =
        employeeRepository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);

    if (request.isPreferred()) {
      contactPointRepository
          .findByEmployeeId(employeeId)
          .forEach(
              cp -> {
                if (cp.isPreferred()) {
                  cp.setPreferred(false);
                  contactPointRepository.save(cp);
                }
              });
    }

    ContactPoint contactPoint = employeeMapper.toContactPoint(request, employee);

    return employeeMapper.contactPointToContactPointResponseDTO(
        contactPointRepository.save(contactPoint));
  }

  @Transactional
  public PostalAddressDTO updateAddress(UUID employeeId, PostalAddressDTO request) {
    Employee employee =
        employeeRepository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);

    PostalAddress address =
        postalAddressRepository
            .findByEmployeeId(employeeId)
            .orElseGet(() -> employeeMapper.toPostalAddress(request, employee));

    employeeMapper.updatePostalAddressFromDTO(request, address);

    return employeeMapper.postalAddressToPostalAddressDTO(postalAddressRepository.save(address));
  }
}
