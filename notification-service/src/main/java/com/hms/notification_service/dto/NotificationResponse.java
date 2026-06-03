package com.hms.notification_service.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String sourceService,
    String title,
    String message,
    Instant createdAt,
    Instant readAt) {}
