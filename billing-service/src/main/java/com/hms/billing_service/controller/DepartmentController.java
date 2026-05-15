package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.department.CreateDepartmentRequestDTO;
import com.hms.billing_service.dto.department.DepartmentResponseDTO;
import com.hms.billing_service.dto.department.UpdateDepartmentRequestDTO;
import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Billing Departments",
    description = "Manage billing departments (Reception, Medical Service)")
public class DepartmentController {

  private final DepartmentService departmentService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a new billing department")
  public ResponseEntity<DepartmentResponseDTO> createDepartment(
      @Valid @RequestBody CreateDepartmentRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update department details")
  public ResponseEntity<DepartmentResponseDTO> updateDepartment(
      @PathVariable UUID id, @Valid @RequestBody UpdateDepartmentRequestDTO request) {
    return ResponseEntity.ok(departmentService.update(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get department by ID")
  public ResponseEntity<DepartmentResponseDTO> getDepartment(@PathVariable UUID id) {
    return ResponseEntity.ok(departmentService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "List departments with optional section filter")
  public ResponseEntity<PageResponse<DepartmentResponseDTO>> listDepartments(
      @RequestParam(required = false) BillingSectionType section,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    if (section != null) {
      return ResponseEntity.ok(departmentService.getBySection(section, pageable));
    }
    return ResponseEntity.ok(departmentService.getAll(pageable));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete department")
  public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
    departmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
