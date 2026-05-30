package com.hms.analytics_service.repository;

import com.hms.analytics_service.enums.MetricBucketType;
import com.hms.analytics_service.model.MetricAggregate;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricAggregateRepository
    extends JpaRepository<MetricAggregate, UUID>, JpaSpecificationExecutor<MetricAggregate> {
  Optional<MetricAggregate>
      findByMetricNameAndBucketTypeAndBucketStartAndDimensionKeyAndDimensionValue(
          String metricName,
          MetricBucketType bucketType,
          Instant bucketStart,
          String dimensionKey,
          String dimensionValue);
}
