package com.hms.hr_service.controller;

import com.hms.hr_service.dto.Department.CreateDepartmentRequestDTO;
import com.hms.hr_service.dto.Department.DepartmentResponseDTO;
import com.hms.hr_service.dto.Department.UpdateDepartmentRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
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

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Create a new department in a medical organization")
  public ResponseEntity<DepartmentResponseDTO> createDepartment(
      @Valid @RequestBody CreateDepartmentRequestDTO request, AuthenticatedPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(departmentService.create(request, principal));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Update department details")
  public ResponseEntity<DepartmentResponseDTO> updateDepartment(
      @PathVariable UUID id, @Valid @RequestBody UpdateDepartmentRequestDTO request) {
    return ResponseEntity.ok(departmentService.update(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get department by ID")
  public ResponseEntity<DepartmentResponseDTO> getDepartment(@PathVariable UUID id) {
    return ResponseEntity.ok(departmentService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "List departments for a medical organization")
  public ResponseEntity<PageResponse<DepartmentResponseDTO>> listDepartments(
      @RequestParam UUID organizationId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(departmentService.getByOrganization(organizationId, pageable));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Delete department")
  public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
    departmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
