package com.hms.analytics_service.service;

import com.hms.analytics_service.dto.AnalyticsEventMessage;
import com.hms.analytics_service.dto.MetricAggregateResponse;
import com.hms.analytics_service.dto.PageResponse;
import com.hms.analytics_service.enums.MetricBucketType;
import com.hms.analytics_service.mapper.AnalyticsMapper;
import com.hms.analytics_service.model.MetricAggregate;
import com.hms.analytics_service.repository.MetricAggregateRepository;
import com.hms.analytics_service.specification.MetricAggregateSpecifications;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricAggregateService {

  private final MetricAggregateRepository repository;
  private final AnalyticsMapper mapper;

  @Transactional
  public void record(AnalyticsEventMessage event) {
    for (MetricBucketType bucketType : MetricBucketType.values()) {
      Bucket bucket = bucket(event.occurredAt(), bucketType);
      String metricName =
          event.metricName() == null || event.metricName().isBlank()
              ? event.eventType()
              : event.metricName();
      String dimensionKey = valueOrDefault(event.dimensionKey(), "source_service");
      String dimensionValue = valueOrDefault(event.dimensionValue(), event.sourceService());

      MetricAggregate aggregate =
          repository
              .findByMetricNameAndBucketTypeAndBucketStartAndDimensionKeyAndDimensionValue(
                  metricName, bucketType, bucket.start(), dimensionKey, dimensionValue)
              .orElseGet(
                  () -> newAggregate(metricName, bucketType, bucket, dimensionKey, dimensionValue));

      aggregate.setEventCount(aggregate.getEventCount() + 1);
      aggregate.setNumericTotal(
          aggregate
              .getNumericTotal()
              .add(event.numericValue() == null ? BigDecimal.ZERO : event.numericValue()));
      repository.save(aggregate);
    }
  }

  @Transactional(readOnly = true)
  public PageResponse<MetricAggregateResponse> search(
      String metricName,
      MetricBucketType bucketType,
      String dimensionKey,
      String dimensionValue,
      Instant from,
      Instant to,
      Pageable pageable) {
    return PageResponse.from(
        repository
            .findAll(
                MetricAggregateSpecifications.search(
                    metricName, bucketType, dimensionKey, dimensionValue, from, to),
                pageable)
            .map(mapper::toResponse));
  }

  private MetricAggregate newAggregate(
      String metricName,
      MetricBucketType bucketType,
      Bucket bucket,
      String dimensionKey,
      String dimensionValue) {
    MetricAggregate aggregate = new MetricAggregate();
    aggregate.setMetricName(metricName);
    aggregate.setBucketType(bucketType);
    aggregate.setBucketStart(bucket.start());
    aggregate.setBucketEnd(bucket.end());
    aggregate.setDimensionKey(dimensionKey);
    aggregate.setDimensionValue(dimensionValue);
    return aggregate;
  }

  private Bucket bucket(Instant occurredAt, MetricBucketType bucketType) {
    ZonedDateTime dateTime = occurredAt.atZone(ZoneOffset.UTC);
    ZonedDateTime start =
        switch (bucketType) {
          case HOUR -> dateTime.truncatedTo(ChronoUnit.HOURS);
          case DAY -> dateTime.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          case MONTH -> dateTime.toLocalDate().withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC);
        };
    ZonedDateTime end =
        switch (bucketType) {
          case HOUR -> start.plusHours(1);
          case DAY -> start.plusDays(1);
          case MONTH -> start.plusMonths(1);
        };
    return new Bucket(start.toInstant(), end.toInstant());
  }

  private String valueOrDefault(String value, String defaultValue) {
    return value == null || value.isBlank() ? defaultValue : value;
  }

  private record Bucket(Instant start, Instant end) {}
}
