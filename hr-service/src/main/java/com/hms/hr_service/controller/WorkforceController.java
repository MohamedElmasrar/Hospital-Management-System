package com.hms.hr_service.controller;

import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.service.WorkforceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workforce")
@RequiredArgsConstructor
public class WorkforceController {
  private final WorkforceService workforceService;

  @PostMapping("/employees/{employeeId}/credentials")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.CredentialResponse> addCredential(
      @PathVariable UUID employeeId, @Valid @RequestBody WorkforceDTO.CredentialRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.addCredential(employeeId, request));
  }

  @GetMapping("/credentials/expiring")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<List<WorkforceDTO.CredentialResponse>> expiringCredentials(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(workforceService.expiringCredentials(days));
  }

  @PostMapping("/shifts")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.ShiftResponse> createShift(@Valid @RequestBody WorkforceDTO.ShiftRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createShift(request));
  }

  @GetMapping("/employees/{employeeId}/shifts")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<List<WorkforceDTO.ShiftResponse>> shifts(
      @PathVariable UUID employeeId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
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
  public ResponseEntity<WorkforceDTO.LeaveResponse> requestLeave(@Valid @RequestBody WorkforceDTO.LeaveRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.requestLeave(request));
  }

  @PatchMapping("/leave/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.LeaveResponse> decideLeave(
      @PathVariable UUID id, @Valid @RequestBody WorkforceDTO.LeaveDecisionRequest request) {
    return ResponseEntity.ok(workforceService.decideLeave(id, request));
  }

  @PostMapping("/attendance")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.AttendanceResponse> attendance(
      @Valid @RequestBody WorkforceDTO.AttendanceRequest request) {
    return ResponseEntity.ok(workforceService.recordAttendance(request));
  }

  @PostMapping("/payroll/calculate")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  public ResponseEntity<WorkforceDTO.PayrollResponse> payroll(
      @Valid @RequestBody WorkforceDTO.PayrollRequest request) {
    return ResponseEntity.ok(workforceService.payroll(request));
  }
}
