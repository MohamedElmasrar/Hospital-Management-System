package com.hms.hr_service.security;

import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.repository.EmployeeAssignmentRepository;
import com.hms.hr_service.repository.EmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HrAccessService {
  private final EmployeeRepository employeeRepository;
  private final EmployeeAssignmentRepository assignmentRepository;

  public UUID ownEmployeeId(AuthenticatedPrincipal principal) {
    if (principal == null || principal.userId() == null)
      throw new AccessDeniedException("No employee identity");
    return employeeRepository
        .findByUserId(principal.userId())
        .map(Employee::getId)
        .orElseThrow(() -> new AccessDeniedException("No linked employee"));
  }

  public Set<UUID> managedOrganizationIds(AuthenticatedPrincipal principal) {
    if (principal == null || principal.role() != UserRole.HR_HEAD)
      throw new AccessDeniedException("HR Head required");
    return assignmentRepository.findByEmployeeId(ownEmployeeId(principal)).stream()
        .filter(this::active)
        .map(assignment -> assignment.getDepartment().getMedicalOrganization().getId())
        .collect(Collectors.toSet());
  }

  public void requireOrganization(AuthenticatedPrincipal principal, UUID organizationId) {
    if (principal != null && principal.role() == UserRole.ADMIN) return;
    if (!managedOrganizationIds(principal).contains(organizationId))
      throw new AccessDeniedException("Organization is outside HR scope");
  }

  public void requireEmployee(AuthenticatedPrincipal principal, UUID employeeId) {
    if (principal != null && principal.role() == UserRole.ADMIN) return;
    if (principal != null
        && principal.role() != UserRole.HR_HEAD
        && employeeId.equals(ownEmployeeId(principal))) return;
    Set<UUID> organizations = managedOrganizationIds(principal);
    boolean allowed =
        assignmentRepository.findByEmployeeId(employeeId).stream()
            .filter(this::active)
            .anyMatch(
                assignment ->
                    organizations.contains(
                        assignment.getDepartment().getMedicalOrganization().getId()));
    if (!allowed) throw new AccessDeniedException("Employee is outside HR scope");
  }

  private boolean active(EmployeeAssignment assignment) {
    LocalDate today = LocalDate.now();
    return !day(assignment.getStartDate()).isAfter(today)
        && (assignment.getEndDate() == null || !day(assignment.getEndDate()).isBefore(today));
  }

  private LocalDate day(Date date) {
    return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
