package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class QueuePriorityLevelDTO {

  public record Request(
      @NotBlank(message = "Code is required") String code,
      @NotBlank(message = "Name is required") String name,
      @NotNull(message = "Weight is required") Integer weight,
      String badgeColorHex,
      Boolean isActive) {}

  public record Response(
      UUID id, String code, String name, Integer weight, String badgeColorHex, Boolean isActive) {}
}
