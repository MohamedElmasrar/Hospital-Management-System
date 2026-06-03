package com.hms.hr_service.controller;

import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.payroll.PayrollDTO;
import com.hms.hr_service.enums.PayrollStatus;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.service.PayrollService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payroll")
@Tag(name = "Payroll")
@PreAuthorize("hasAnyRole('ADMIN','HR_HEAD')")
public class PayrollController {
  private final PayrollService service;

  @GetMapping("/settings")
  @Operation(operationId = "getPayrollSettings")
  public PayrollDTO.SettingsResponse settings(
      @RequestParam UUID organizationId,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.settings(organizationId, principal);
  }

  @PutMapping("/settings")
  @Operation(operationId = "savePayrollSettings")
  public PayrollDTO.SettingsResponse saveSettings(
      @RequestParam UUID organizationId,
      @Valid @RequestBody PayrollDTO.SettingsRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.saveSettings(organizationId, request, principal);
  }

  @GetMapping("/compensations/{employeeId}")
  @Operation(operationId = "getEmployeeCompensation")
  public PayrollDTO.CompensationResponse compensation(
      @RequestParam UUID organizationId,
      @PathVariable UUID employeeId,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.compensation(organizationId, employeeId, principal);
  }

  @PutMapping("/compensations/{employeeId}")
  @Operation(operationId = "saveEmployeeCompensation")
  public PayrollDTO.CompensationResponse saveCompensation(
      @RequestParam UUID organizationId,
      @PathVariable UUID employeeId,
      @Valid @RequestBody PayrollDTO.CompensationRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.saveCompensation(organizationId, employeeId, request, principal);
  }

  @GetMapping("/records")
  @Operation(operationId = "listPayrollRecords")
  public PageResponse<PayrollDTO.RecordResponse> list(
      @RequestParam UUID organizationId,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) PayrollStatus status,
      @ParameterObject @PageableDefault(size = 10, sort = "period", direction = Sort.Direction.DESC)
          Pageable pageable,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.list(organizationId, month, status, pageable, principal);
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  @Operation(operationId = "listMyPayrollRecords")
  public PageResponse<PayrollDTO.RecordResponse> mine(
      @ParameterObject @PageableDefault(size = 10, sort = "period", direction = Sort.Direction.DESC)
          Pageable pageable,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.mine(pageable, principal);
  }

  @PostMapping("/records")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(operationId = "createPayrollRecord")
  public PayrollDTO.RecordResponse create(
      @RequestParam UUID organizationId,
      @Valid @RequestBody PayrollDTO.RecordRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.create(organizationId, request, principal);
  }

  @PutMapping("/records/{id}")
  @Operation(operationId = "recalculatePayrollRecord")
  public PayrollDTO.RecordResponse recalculate(
      @PathVariable UUID id,
      @Valid @RequestBody PayrollDTO.RecordRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.recalculate(id, request, principal);
  }

  @PostMapping("/records/{id}/approve")
  @Operation(operationId = "approvePayrollRecord")
  public PayrollDTO.RecordResponse approve(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.approve(id, principal);
  }

  @PostMapping("/records/{id}/pay")
  @Operation(operationId = "markPayrollRecordPaid")
  public PayrollDTO.RecordResponse paid(
      @PathVariable UUID id, @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.markPaid(id, principal);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> error(ResponseStatusException error) {
    return ResponseEntity.status(error.getStatusCode())
        .body(
            Map.of(
                "error", error.getReason() == null ? "Payroll request failed" : error.getReason()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> duplicate(DataIntegrityViolationException error) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            Map.of(
                "error",
                "A conflicting payroll or salary setting already exists. Refresh and try again."));
  }
}
