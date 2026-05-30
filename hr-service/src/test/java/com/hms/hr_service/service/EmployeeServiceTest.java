package com.hms.hr_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hms.hr_service.dto.Employee.AssignEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.ContactPointResponseDTO;
import com.hms.hr_service.dto.Employee.CreateContactPointRequestDTO;
import com.hms.hr_service.dto.Employee.CreateEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.EmployeeAssignmentResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeDetailResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeResponseDTO;
import com.hms.hr_service.dto.Employee.PostalAddressDTO;
import com.hms.hr_service.dto.Employee.UpdateEmployeeRequestDTO;
import com.hms.hr_service.enums.AddressType;
import com.hms.hr_service.enums.ContactType;
import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
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
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @Mock private EmployeeRepository employeeRepository;
  @Mock private ContactPointRepository contactPointRepository;
  @Mock private PostalAddressRepository postalAddressRepository;
  @Mock private EmployeeAssignmentRepository employeeAssignmentRepository;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private EmployeeMapper employeeMapper;
  @Mock private com.hms.hr_service.messaging.AnalyticsEventPublisher analyticsEventPublisher;

  @InjectMocks private EmployeeService employeeService;

  private UUID employeeId;
  private Employee employee;

  @BeforeEach
  void setUp() {
    employeeId = UUID.randomUUID();
    employee = new Employee();
    employee.setId(employeeId);
    employee.setIdentifier("EMP-001");
    employee.setGivenName("John");
    employee.setFamilyName("Doe");
    employee.setEmploymentStatus(EmploymentStatus.Active);
  }

  @Test
  void create_validRequest_createsAndReturnsEmployee() {
    CreateEmployeeRequestDTO request =
        new CreateEmployeeRequestDTO(
            "EMP-001",
            "John",
            null,
            "Doe",
            "Dr.",
            new Date(),
            GenderType.Male,
            EmploymentStatus.Active,
            "john@example.com",
            "1234567890",
            ContactType.Work,
            "123 Main St",
            "City",
            "State",
            "12345",
            "US",
            AddressType.Work);

    EmployeeResponseDTO responseDTO =
        new EmployeeResponseDTO(
            employeeId,
            "EMP-001",
            "John",
            null,
            "Doe",
            "Dr.",
            null,
            GenderType.Male,
            EmploymentStatus.Active,
            new Date(),
            new Date());

    given(employeeRepository.existsByIdentifier("EMP-001")).willReturn(false);
    given(employeeMapper.createEmployeeRequestDTOToEmployee(request)).willReturn(employee);
    given(employeeRepository.save(employee)).willReturn(employee);
    given(employeeMapper.createEmployeeRequestDTOToContactPoint(request, employee))
        .willReturn(new ContactPoint());
    given(employeeMapper.createEmployeeRequestDTOToPostalAddress(request, employee))
        .willReturn(new PostalAddress());
    given(employeeMapper.employeeToEmployeeResponseDTO(employee)).willReturn(responseDTO);

    EmployeeResponseDTO result = employeeService.create(request);

    assertThat(result).isNotNull();
    assertThat(result.identifier()).isEqualTo("EMP-001");
    verify(employeeRepository).save(employee);
    verify(analyticsEventPublisher).publish("EMPLOYEE_CREATED", "EMPLOYEE", employeeId);
    verify(contactPointRepository).save(any(ContactPoint.class));
    verify(postalAddressRepository).save(any(PostalAddress.class));
  }

  @Test
  void create_duplicateIdentifier_throwsResourceAlreadyExistsException() {
    CreateEmployeeRequestDTO request =
        new CreateEmployeeRequestDTO(
            "EMP-001",
            "John",
            null,
            "Doe",
            null,
            null,
            null,
            EmploymentStatus.Active,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    given(employeeRepository.existsByIdentifier("EMP-001")).willReturn(true);

    assertThatThrownBy(() -> employeeService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  void getById_existingEmployee_returnsDetail() {
    EmployeeDetailResponseDTO detailDTO =
        new EmployeeDetailResponseDTO(
            employeeId,
            "EMP-001",
            "John",
            null,
            "Doe",
            null,
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            Collections.emptyList());

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(contactPointRepository.findByEmployeeId(employeeId)).willReturn(Collections.emptyList());
    given(postalAddressRepository.findByEmployeeId(employeeId)).willReturn(Optional.empty());
    given(employeeAssignmentRepository.findByEmployeeId(employeeId))
        .willReturn(Collections.emptyList());
    given(
            employeeMapper.toEmployeeDetailResponseDTO(
                employee, Collections.emptyList(), null, Collections.emptyList()))
        .willReturn(detailDTO);

    EmployeeDetailResponseDTO result = employeeService.getById(employeeId);

    assertThat(result).isNotNull();
    assertThat(result.identifier()).isEqualTo("EMP-001");
    assertThat(result.givenName()).isEqualTo("John");
  }

  @Test
  void getById_notFound_throwsEmployeeNotFoundException() {
    given(employeeRepository.findById(employeeId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> employeeService.getById(employeeId))
        .isInstanceOf(EmployeeNotFoundException.class);
  }

  @Test
  void update_validRequest_updatesEmployee() {
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "Jane", null, "Smith", null, null, null, EmploymentStatus.Active);
    EmployeeResponseDTO responseDTO =
        new EmployeeResponseDTO(
            employeeId,
            "EMP-001",
            "Jane",
            null,
            "Smith",
            null,
            null,
            null,
            EmploymentStatus.Active,
            new Date(),
            new Date());

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(employeeRepository.save(employee)).willReturn(employee);
    given(employeeMapper.employeeToEmployeeResponseDTO(employee)).willReturn(responseDTO);

    EmployeeResponseDTO result = employeeService.update(employeeId, request);

    assertThat(result.givenName()).isEqualTo("Jane");
    verify(employeeMapper).updateEmployeeFromDTO(request, employee);
  }

  @Test
  void assignToDepartment_validRequest_createsAssignment() {
    UUID deptId = UUID.randomUUID();
    Department dept = new Department();
    dept.setId(deptId);
    dept.setName("Surgery");

    AssignEmployeeRequestDTO request =
        new AssignEmployeeRequestDTO(deptId, "Surgeon", new Date(), null);
    EmployeeAssignment savedAssignment = new EmployeeAssignment();

    EmployeeAssignmentResponseDTO responseDTO =
        new EmployeeAssignmentResponseDTO(
            UUID.randomUUID(),
            employeeId,
            "EMP-001",
            "John Doe",
            deptId,
            "Surgery",
            "Surgeon",
            new Date(),
            null);

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(departmentRepository.findById(deptId)).willReturn(Optional.of(dept));
    given(employeeMapper.toEmployeeAssignment(request, employee, dept)).willReturn(savedAssignment);
    given(employeeAssignmentRepository.save(savedAssignment)).willReturn(savedAssignment);
    given(employeeMapper.employeeAssignmentToEmployeeAssignmentResponseDTO(savedAssignment))
        .willReturn(responseDTO);

    EmployeeAssignmentResponseDTO result = employeeService.assignToDepartment(employeeId, request);

    assertThat(result).isNotNull();
    assertThat(result.jobTitle()).isEqualTo("Surgeon");
    verify(employeeAssignmentRepository).save(savedAssignment);
  }

  @Test
  void assignToDepartment_departmentNotFound_throwsDepartmentNotFoundException() {
    UUID deptId = UUID.randomUUID();
    AssignEmployeeRequestDTO request =
        new AssignEmployeeRequestDTO(deptId, "Surgeon", new Date(), null);

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(departmentRepository.findById(deptId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> employeeService.assignToDepartment(employeeId, request))
        .isInstanceOf(DepartmentNotFoundException.class);
  }

  @Test
  void addContactPoint_validRequest_savesContactPoint() {
    CreateContactPointRequestDTO request =
        new CreateContactPointRequestDTO(ContactType.Mobile, "555-1234", null, true);

    ContactPoint contactPointToSave = new ContactPoint();
    ContactPoint savedCp = new ContactPoint();
    ContactPointResponseDTO responseDTO =
        new ContactPointResponseDTO(UUID.randomUUID(), ContactType.Mobile, "555-1234", null, true);

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(contactPointRepository.findByEmployeeId(employeeId)).willReturn(Collections.emptyList());
    given(employeeMapper.toContactPoint(request, employee)).willReturn(contactPointToSave);
    given(contactPointRepository.save(contactPointToSave)).willReturn(savedCp);
    given(employeeMapper.contactPointToContactPointResponseDTO(savedCp)).willReturn(responseDTO);

    ContactPointResponseDTO result = employeeService.addContactPoint(employeeId, request);

    assertThat(result).isNotNull();
    assertThat(result.telephone()).isEqualTo("555-1234");
    verify(contactPointRepository).save(contactPointToSave);
  }

  @Test
  void updateAddress_validRequest_updatesAddress() {
    PostalAddressDTO request =
        new PostalAddressDTO(
            UUID.randomUUID(), AddressType.Work, "123 Main St", "City", "State", "12345", "US");
    PostalAddress address = new PostalAddress();

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(postalAddressRepository.findByEmployeeId(employeeId)).willReturn(Optional.of(address));
    given(postalAddressRepository.save(address)).willReturn(address);
    given(employeeMapper.postalAddressToPostalAddressDTO(address)).willReturn(request);

    PostalAddressDTO result = employeeService.updateAddress(employeeId, request);

    assertThat(result).isNotNull();
    verify(employeeMapper).updatePostalAddressFromDTO(request, address);
  }

  @Test
  void updateAddress_whenAddressNotExists_createsAndUpdatesAddress() {
    PostalAddressDTO request =
        new PostalAddressDTO(
            UUID.randomUUID(), AddressType.Work, "123 Main St", "City", "State", "12345", "US");
    PostalAddress address = new PostalAddress();

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(postalAddressRepository.findByEmployeeId(employeeId)).willReturn(Optional.empty());
    given(employeeMapper.toPostalAddress(request, employee)).willReturn(address);
    given(postalAddressRepository.save(address)).willReturn(address);
    given(employeeMapper.postalAddressToPostalAddressDTO(address)).willReturn(request);

    PostalAddressDTO result = employeeService.updateAddress(employeeId, request);

    assertThat(result).isNotNull();
    verify(employeeMapper).updatePostalAddressFromDTO(request, address);
  }
}
