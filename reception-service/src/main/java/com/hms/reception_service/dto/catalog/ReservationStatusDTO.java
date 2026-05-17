package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class ReservationStatusDTO {

  public record Request(
      @NotBlank(message = "Code is required") String code,
      @NotBlank(message = "Name is required") String name,
      String badgeColorHex,
      Boolean allowsQueueing,
      Boolean isTerminal,
      Boolean isActive) {}

  public record Response(
      UUID id,
      String code,
      String name,
      String badgeColorHex,
      Boolean allowsQueueing,
      Boolean isTerminal,
      Boolean isActive) {}
}
