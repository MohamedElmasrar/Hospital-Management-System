package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

public class DiagnosticModalityDTO {

  public record Request(
      @NotBlank(message = "Code is required") String code,
      @NotBlank(message = "Name is required") String name,
      @NotNull(message = "Department ID is required") UUID departmentId,
      Integer defaultDurationMinutes,
      Boolean requiresContrastScreening,
      Boolean requiresSafetyChecklist,
      Boolean isActive) {}

  public record Response(
      UUID id,
      String code,
      String name,
      UUID departmentId,
      Integer defaultDurationMinutes,
      Boolean requiresContrastScreening,
      Boolean requiresSafetyChecklist,
      Boolean isActive,
      Date dateCreated) {}
}
