package com.hms.hr_service.dto.workforce;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public class WorkforceDTO {
  public record CredentialRequest(
      @NotBlank String credentialType, @NotBlank String credentialNumber, @NotNull LocalDate expiresOn) {}
  public record CredentialResponse(UUID id, UUID employeeId, String credentialType, String credentialNumber, LocalDate expiresOn) {}
  public record ShiftRequest(
      @NotNull UUID employeeId, UUID departmentId, @NotNull LocalDate shiftDate,
      @NotNull LocalTime startsAt, @NotNull LocalTime endsAt) {}
  public record ShiftResponse(UUID id, UUID employeeId, UUID departmentId, LocalDate shiftDate, LocalTime startsAt, LocalTime endsAt) {}
  public record LeaveRequest(@NotNull UUID employeeId, @NotNull LocalDate startsOn, @NotNull LocalDate endsOn, @NotBlank String reason) {}
  public record LeaveDecisionRequest(@NotBlank String status, String decisionReason) {}
  public record LeaveResponse(UUID id, UUID employeeId, LocalDate startsOn, LocalDate endsOn, String reason, String status, String decisionReason) {}
  public record AttendanceRequest(@NotNull UUID employeeId, @NotNull LocalDate attendanceDate, boolean checkIn, boolean checkOut) {}
  public record AttendanceResponse(UUID id, UUID employeeId, LocalDate attendanceDate, LocalDateTime checkedInAt, LocalDateTime checkedOutAt) {}
  public record PayrollRequest(
      @NotNull UUID employeeId, @NotNull YearMonth month, @NotNull @DecimalMin("0.00") BigDecimal baseSalary,
      @NotNull @DecimalMin("0.00") BigDecimal overtime, @NotNull @DecimalMin("0.00") BigDecimal deductions) {}
  public record PayrollResponse(UUID employeeId, YearMonth month, BigDecimal baseSalary, BigDecimal overtime, BigDecimal deductions, BigDecimal netSalary) {}
}
