package com.hms.notification_service.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
    @NotNull UUID eventId,
    @NotNull UUID recipientId,
    @NotBlank @Size(max = 100) String sourceService,
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 500) String message,
    @NotNull Instant occurredAt) {}
