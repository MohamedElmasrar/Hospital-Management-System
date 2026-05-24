package com.hms.reception_service.dto.reminder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReminderDTO {
  public record CreateRequest(
      @NotNull UUID reservationId,
      @NotBlank String channel,
      @NotBlank String recipient,
      @NotNull LocalDateTime scheduledFor) {}

  public record Response(
      UUID id,
      UUID reservationId,
      String channel,
      String recipient,
      LocalDateTime scheduledFor,
      LocalDateTime sentAt) {}
}
