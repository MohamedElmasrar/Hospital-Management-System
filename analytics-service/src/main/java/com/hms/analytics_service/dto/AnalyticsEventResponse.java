package com.hms.analytics_service.dto;

import com.hms.analytics_service.enums.EventProcessingStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AnalyticsEventResponse(
    UUID id,
    UUID eventId,
    String eventType,
    String sourceService,
    String aggregateType,
    String aggregateId,
    Integer schemaVersion,
    Instant occurredAt,
    Instant receivedAt,
    EventProcessingStatus processingStatus,
    String payload,
    String processingError) {}
