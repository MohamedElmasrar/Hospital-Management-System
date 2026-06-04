package com.hms.reception_service.dto.notification;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationEvent(
    UUID eventId,
    UUID recipientId,
    String sourceService,
    String title,
    String message,
    Instant occurredAt) {}
