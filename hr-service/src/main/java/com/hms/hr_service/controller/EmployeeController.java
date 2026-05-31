package com.hms.hr_service.controller;

import com.hms.hr_service.dto.Employee.AssignEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.ContactPointResponseDTO;
import com.hms.hr_service.dto.Employee.CreateAssignedEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.CreateContactPointRequestDTO;
import com.hms.hr_service.dto.Employee.CreateEmployeeRequestDTO;
import com.hms.hr_service.dto.Employee.EmployeeAssignmentResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeDetailResponseDTO;
import com.hms.hr_service.dto.Employee.EmployeeResponseDTO;
import com.hms.hr_service.dto.Employee.PostalAddressDTO;
import com.hms.hr_service.dto.Employee.UpdateEmployeeRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.security.HrAccessService;
import com.hms.hr_service.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;
  private final HrAccessService hrAccessService;

  @PostMapping("/with-assignment")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<EmployeeResponseDTO> createAssignedEmployee(
      @Valid @RequestBody CreateAssignedEmployeeRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(employeeService.createAssigned(request, principal));
  }

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST', 'HR_HEAD')")
  @Operation(summary = "Get the authenticated employee profile")
  public ResponseEntity<EmployeeDetailResponseDTO> getMyProfile(
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return ResponseEntity.ok(employeeService.getByUserId(principal.userId()));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a new employee")
  public ResponseEntity<EmployeeResponseDTO> createEmployee(
      @Valid @RequestBody CreateEmployeeRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'HR_HEAD')")
  @Operation(summary = "Get employee details with contact and address info")
  public ResponseEntity<EmployeeDetailResponseDTO> getEmployee(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, id);
    return ResponseEntity.ok(employeeService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Search employees with pagination")
  public ResponseEntity<PageResponse<EmployeeResponseDTO>> searchEmployees(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) EmploymentStatus status,
      @RequestParam(required = false) UUID organizationId,
      @Parameter(hidden = true) AuthenticatedPrincipal principal,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(
        employeeService.search(search, status, organizationId, pageable, principal));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'HR_HEAD') or (hasRole('EMPLOYEE') and #id.toString() == authentication.name)")
  @Operation(summary = "Update employee details")
  public ResponseEntity<EmployeeResponseDTO> updateEmployee(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateEmployeeRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, id);
    return ResponseEntity.ok(employeeService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Delete employee")
  public ResponseEntity<Void> deleteEmployee(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, id);
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/assignments")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Assign employee to department")
  public ResponseEntity<EmployeeAssignmentResponseDTO> assignToDepartment(
      @PathVariable UUID id,
      @Valid @RequestBody AssignEmployeeRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, id);
    hrAccessService.requireOrganization(
        principal, employeeService.departmentOrganizationId(request.departmentId()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(employeeService.assignToDepartment(id, request));
  }

  @GetMapping("/{id}/assignments")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get employee department assignments")
  public ResponseEntity<List<EmployeeAssignmentResponseDTO>> getAssignments(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, id);
    return ResponseEntity.ok(employeeService.getAssignments(id));
  }

  @PostMapping("/{id}/contact-points")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'HR_HEAD') or (hasRole('EMPLOYEE') and #id.toString() == authentication.name)")
  @Operation(summary = "Add contact point for employee")
  public ResponseEntity<ContactPointResponseDTO> addContactPoint(
      @PathVariable UUID id, @Valid @RequestBody CreateContactPointRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(employeeService.addContactPoint(id, request));
  }

  @PutMapping("/{id}/address")
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'HR_HEAD') or (hasRole('EMPLOYEE') and #id.toString() == authentication.name)")
  @Operation(summary = "Update or add postal address for employee")
  public ResponseEntity<PostalAddressDTO> updateAddress(
      @PathVariable UUID id, @Valid @RequestBody PostalAddressDTO request) {
    return ResponseEntity.ok(employeeService.updateAddress(id, request));
  }
}
