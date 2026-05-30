package com.hms.analytics_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnalyticsEventMessage(
    @NotNull UUID eventId,
    @NotBlank String eventType,
    @NotBlank String sourceService,
    String aggregateType,
    String aggregateId,
    Integer schemaVersion,
    @NotNull Instant occurredAt,
    @NotBlank String payload,
    String metricName,
    BigDecimal numericValue,
    String dimensionKey,
    String dimensionValue) {}
