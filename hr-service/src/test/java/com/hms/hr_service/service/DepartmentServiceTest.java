package com.hms.hr_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hms.hr_service.dto.Department.CreateDepartmentRequestDTO;
import com.hms.hr_service.dto.Department.DepartmentResponseDTO;
import com.hms.hr_service.dto.Department.UpdateDepartmentRequestDTO;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.exception.DepartmentNotFoundException;
import com.hms.hr_service.exception.MedicalOrganizationNotFoundException;
import com.hms.hr_service.mapper.DepartmentMapper;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.DepartmentRepository;
import com.hms.hr_service.repository.MedicalOrganizationRepository;
import com.hms.hr_service.security.AuthenticatedPrincipal;
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
class DepartmentServiceTest {

  @Mock private DepartmentRepository departmentRepository;
  @Mock private MedicalOrganizationRepository medicalOrganizationRepository;
  @Mock private DepartmentMapper departmentMapper;

  @InjectMocks private DepartmentService departmentService;

  private UUID orgId;
  private UUID adminId;
  private AuthenticatedPrincipal principal;
  private MedicalOrganization organization;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    adminId = UUID.randomUUID();
    principal = new AuthenticatedPrincipal(adminId, UserRole.ADMIN);

    organization = new MedicalOrganization();
    organization.setId(orgId);
    organization.setAdminId(adminId);
    organization.setName("General Hospital");
  }

  @Test
  void create_validRequest_createsAndReturnsDepartment() {
    CreateDepartmentRequestDTO request =
        new CreateDepartmentRequestDTO(orgId, null, "Cardiology", "Cardiology Department");

    Department department = new Department();
    department.setName("Cardiology");

    Department savedDepartment = new Department();
    savedDepartment.setId(UUID.randomUUID());
    savedDepartment.setName("Cardiology");

    DepartmentResponseDTO responseDTO =
        new DepartmentResponseDTO(
            savedDepartment.getId(),
            orgId,
            "General Hospital",
            null,
            null,
            "Cardiology",
            "Cardiology Department",
            new Date());

    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.of(organization));
    given(departmentMapper.createDepartmentRequestDTOToDepartment(request, organization))
        .willReturn(department);
    given(departmentRepository.save(department)).willReturn(savedDepartment);
    given(departmentMapper.departmentToDepartmentResponseDTO(savedDepartment))
        .willReturn(responseDTO);

    DepartmentResponseDTO result = departmentService.create(request, principal);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Cardiology");
    verify(departmentRepository).save(department);
  }

  @Test
  void create_orgNotFound_throwsMedicalOrganizationNotFoundException() {
    CreateDepartmentRequestDTO request =
        new CreateDepartmentRequestDTO(orgId, null, "Cardiology", null);

    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> departmentService.create(request, principal))
        .isInstanceOf(MedicalOrganizationNotFoundException.class);
  }

  @Test
  void update_validRequest_updatesDepartment() {
    UUID deptId = UUID.randomUUID();
    UpdateDepartmentRequestDTO request =
        new UpdateDepartmentRequestDTO("New Name", "New Description", null);

    Department department = new Department();
    department.setId(deptId);
    department.setName("Old Name");
    department.setMedicalOrganization(organization);

    DepartmentResponseDTO responseDTO =
        new DepartmentResponseDTO(
            deptId,
            orgId,
            "General Hospital",
            null,
            null,
            "New Name",
            "New Description",
            new Date());

    given(departmentRepository.findById(deptId)).willReturn(Optional.of(department));
    given(departmentRepository.save(department)).willReturn(department);
    given(departmentMapper.departmentToDepartmentResponseDTO(department)).willReturn(responseDTO);

    DepartmentResponseDTO result = departmentService.update(deptId, request, principal);

    assertThat(result.name()).isEqualTo("New Name");
    verify(departmentMapper).updateDepartmentFromDTO(request, department);
    verify(departmentRepository).save(department);
  }

  @Test
  void getById_notFound_throwsDepartmentNotFoundException() {
    UUID deptId = UUID.randomUUID();
    given(departmentRepository.findById(deptId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> departmentService.getById(deptId))
        .isInstanceOf(DepartmentNotFoundException.class);
  }

  @Test
  void delete_validId_deletesDepartment() {
    UUID deptId = UUID.randomUUID();
    Department department = new Department();
    department.setId(deptId);

    given(departmentRepository.findById(deptId)).willReturn(Optional.of(department));

    departmentService.delete(deptId, principal);

    verify(departmentRepository).delete(department);
  }
}
