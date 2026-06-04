package com.hms.analytics_service.dto;

import com.hms.analytics_service.enums.MetricBucketType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MetricAggregateResponse(
    UUID id,
    String metricName,
    MetricBucketType bucketType,
    Instant bucketStart,
    Instant bucketEnd,
    String dimensionKey,
    String dimensionValue,
    Long eventCount,
    BigDecimal numericTotal,
    Instant updatedAt) {}
