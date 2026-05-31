package com.hms.hr_service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.EmployeeAssignmentRepository;
import com.hms.hr_service.repository.EmployeeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class HrAccessServiceTest {
  @Mock private EmployeeRepository employeeRepository;
  @Mock private EmployeeAssignmentRepository assignmentRepository;
  @InjectMocks private HrAccessService accessService;

  @Test
  void activeAssignmentGrantsOnlyItsOrganization() {
    UUID userId = UUID.randomUUID();
    UUID employeeId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    Employee employee = new Employee();
    employee.setId(employeeId);
    MedicalOrganization organization = new MedicalOrganization();
    organization.setId(organizationId);
    Department department = new Department();
    department.setMedicalOrganization(organization);
    EmployeeAssignment active = new EmployeeAssignment();
    active.setDepartment(department);
    active.setStartDate(date(LocalDate.now().minusDays(1)));
    EmployeeAssignment ended = new EmployeeAssignment();
    ended.setDepartment(department);
    ended.setStartDate(date(LocalDate.now().minusDays(10)));
    ended.setEndDate(date(LocalDate.now().minusDays(1)));
    when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));
    when(assignmentRepository.findByEmployeeId(employeeId)).thenReturn(List.of(active, ended));
    AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId, UserRole.HR_HEAD);

    assertThat(accessService.managedOrganizationIds(principal)).containsExactly(organizationId);
    assertThatThrownBy(() -> accessService.requireOrganization(principal, UUID.randomUUID()))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void noActiveAssignmentGivesNoManagementAccess() {
    UUID userId = UUID.randomUUID();
    Employee employee = new Employee();
    employee.setId(UUID.randomUUID());
    when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));
    when(assignmentRepository.findByEmployeeId(employee.getId())).thenReturn(List.of());

    assertThat(
            accessService.managedOrganizationIds(
                new AuthenticatedPrincipal(userId, UserRole.HR_HEAD)))
        .isEmpty();
  }

  private Date date(LocalDate day) {
    return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
