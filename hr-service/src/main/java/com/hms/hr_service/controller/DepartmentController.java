package com.hms.hr_service.controller;

import com.hms.hr_service.dto.Department.CreateDepartmentRequestDTO;
import com.hms.hr_service.dto.Department.DepartmentResponseDTO;
import com.hms.hr_service.dto.Department.UpdateDepartmentRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.security.HrAccessService;
import com.hms.hr_service.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
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
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

  private final DepartmentService departmentService;
  private final HrAccessService hrAccessService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Create a new department in a medical organization")
  public ResponseEntity<DepartmentResponseDTO> createDepartment(
      @Valid @RequestBody CreateDepartmentRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireOrganization(principal, request.medicalOrganizationId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(departmentService.create(request, principal));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Update department details")
  public ResponseEntity<DepartmentResponseDTO> updateDepartment(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateDepartmentRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireOrganization(
        principal, departmentService.getById(id).medicalOrganizationId());
    return ResponseEntity.ok(departmentService.update(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get department by ID")
  public ResponseEntity<DepartmentResponseDTO> getDepartment(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    if (principal.role() == UserRole.HR_HEAD)
      hrAccessService.requireOrganization(
          principal, departmentService.getById(id).medicalOrganizationId());
    return ResponseEntity.ok(departmentService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  @Operation(summary = "List departments for a medical organization")
  public ResponseEntity<PageResponse<DepartmentResponseDTO>> listDepartments(
      @RequestParam UUID organizationId,
      @Parameter(hidden = true) AuthenticatedPrincipal principal,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    if (principal.role() == UserRole.HR_HEAD)
      hrAccessService.requireOrganization(principal, organizationId);
    return ResponseEntity.ok(departmentService.getByOrganization(organizationId, pageable));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Delete department")
  public ResponseEntity<Void> deleteDepartment(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireOrganization(
        principal, departmentService.getById(id).medicalOrganizationId());
    departmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
