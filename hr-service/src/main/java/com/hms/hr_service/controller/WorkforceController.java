package com.hms.hr_service.controller;

import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.security.HrAccessService;
import com.hms.hr_service.service.WorkforceService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workforce")
@RequiredArgsConstructor
public class WorkforceController {
  private final WorkforceService workforceService;
  private final HrAccessService hrAccessService;

  @GetMapping("/organizations")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public List<MedicalOrganizationResponseDTO> organizations(
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return workforceService.managedOrganizations(principal);
  }

  @GetMapping("/shifts")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public PageResponse<WorkforceDTO.ShiftResponse> listShifts(
      @RequestParam(required = false) UUID organizationId,
      @RequestParam(required = false) UUID employeeId,
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @ParameterObject Pageable pageable,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return workforceService.listShifts(organizationId, employeeId, from, to, pageable, principal);
  }

  @GetMapping("/leave")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  public PageResponse<WorkforceDTO.LeaveResponse> listLeave(
      @RequestParam(required = false) UUID organizationId,
      @RequestParam(required = false) UUID employeeId,
      @RequestParam(required = false) String status,
      @ParameterObject Pageable pageable,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    if (principal.role() == UserRole.EMPLOYEE || principal.role() == UserRole.DOCTOR) {
      employeeId = hrAccessService.ownEmployeeId(principal);
      organizationId = null;
    }
    return workforceService.listLeave(organizationId, employeeId, status, pageable, principal);
  }

  @GetMapping("/attendance")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  public PageResponse<WorkforceDTO.AttendanceResponse> listAttendance(
      @RequestParam(required = false) UUID organizationId,
      @RequestParam(required = false) UUID employeeId,
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @ParameterObject Pageable pageable,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    if (principal.role() == UserRole.EMPLOYEE || principal.role() == UserRole.DOCTOR) {
      employeeId = hrAccessService.ownEmployeeId(principal);
      organizationId = null;
    }
    return workforceService.listAttendance(
        organizationId, employeeId, from, to, pageable, principal);
  }

  @PostMapping("/employees/{employeeId}/credentials")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.CredentialResponse> addCredential(
      @PathVariable UUID employeeId, @Valid @RequestBody WorkforceDTO.CredentialRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(workforceService.addCredential(employeeId, request));
  }

  @GetMapping("/credentials/expiring")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<List<WorkforceDTO.CredentialResponse>> expiringCredentials(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(workforceService.expiringCredentials(days));
  }

  @PostMapping("/shifts")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.ShiftResponse> createShift(
      @Valid @RequestBody WorkforceDTO.ShiftRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, request.employeeId());
    if (principal.role() == UserRole.HR_HEAD && request.departmentId() == null)
      throw new IllegalArgumentException("Department required");
    if (request.departmentId() != null)
      hrAccessService.requireOrganization(
          principal, workforceService.departmentOrganizationId(request.departmentId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createShift(request));
  }

  @GetMapping("/employees/{employeeId}/shifts")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<List<WorkforceDTO.ShiftResponse>> shifts(
      @PathVariable UUID employeeId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, employeeId);
    return ResponseEntity.ok(workforceService.shifts(employeeId, from, to));
  }

  @GetMapping("/availability")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<List<WorkforceDTO.ShiftResponse>> availability(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(workforceService.availability(date));
  }

  @PostMapping("/leave")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<WorkforceDTO.LeaveResponse> requestLeave(
      @Valid @RequestBody WorkforceDTO.LeaveRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, request.employeeId());
    return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.requestLeave(request));
  }

  @PatchMapping("/leave/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.LeaveResponse> decideLeave(
      @PathVariable UUID id,
      @Valid @RequestBody WorkforceDTO.LeaveDecisionRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, workforceService.leaveEmployeeId(id));
    return ResponseEntity.ok(workforceService.decideLeave(id, request));
  }

  @PostMapping("/attendance")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.AttendanceResponse> attendance(
      @Valid @RequestBody WorkforceDTO.AttendanceRequest request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    hrAccessService.requireEmployee(principal, request.employeeId());
    return ResponseEntity.ok(workforceService.recordAttendance(request));
  }

  @PostMapping("/payroll/calculate")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.PayrollResponse> payroll(
      @Valid @RequestBody WorkforceDTO.PayrollRequest request) {
    return ResponseEntity.ok(workforceService.payroll(request));
  }
}
