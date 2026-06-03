package com.hms.hr_service.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
    UUID eventId,
    UUID recipientId,
    String sourceService,
    String title,
    String message,
    Instant occurredAt) {}
