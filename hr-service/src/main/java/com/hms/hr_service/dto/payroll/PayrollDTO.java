package com.hms.hr_service.dto.payroll;

import com.hms.hr_service.enums.PayrollStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public final class PayrollDTO {
  private PayrollDTO() {}

  public record SettingsRequest(
      @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
      @Min(1) @Max(28) int payDay,
      @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 2) BigDecimal overtimeHourlyRate) {}

  public record SettingsResponse(
      UUID organizationId, String currency, int payDay, BigDecimal overtimeHourlyRate) {}

  public record CompensationRequest(
      @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 2) BigDecimal baseSalary,
      @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 2) BigDecimal allowance,
      @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 2) BigDecimal deductions) {}

  public record CompensationResponse(
      UUID employeeId,
      String employeeName,
      BigDecimal baseSalary,
      BigDecimal allowance,
      BigDecimal deductions) {}

  public record RecordRequest(
      @NotNull UUID employeeId,
      @NotBlank @Pattern(regexp = "[0-9]{4}-(0[1-9]|1[0-2])") String month,
      @NotNull @DecimalMin("0") @DecimalMax("744") @Digits(integer = 3, fraction = 2)
          BigDecimal overtimeHours,
      @NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 2) BigDecimal extraDeductions) {}

  public record RecordResponse(
      UUID id,
      UUID organizationId,
      UUID employeeId,
      String employeeName,
      LocalDate period,
      String currency,
      LocalDate payDate,
      PayrollStatus status,
      BigDecimal baseSalary,
      BigDecimal allowance,
      BigDecimal overtimeHours,
      BigDecimal overtimeHourlyRate,
      BigDecimal overtimePay,
      BigDecimal extraDeductions,
      BigDecimal deductions,
      BigDecimal grossPay,
      BigDecimal netPay,
      Instant approvedAt,
      Instant paidAt) {}
}
