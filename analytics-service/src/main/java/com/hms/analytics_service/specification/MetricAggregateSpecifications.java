package com.hms.analytics_service.specification;

import com.hms.analytics_service.enums.MetricBucketType;
import com.hms.analytics_service.model.MetricAggregate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class MetricAggregateSpecifications {

  private MetricAggregateSpecifications() {}

  public static Specification<MetricAggregate> search(
      String metricName,
      MetricBucketType bucketType,
      String dimensionKey,
      String dimensionValue,
      Instant from,
      Instant to) {
    List<Specification<MetricAggregate>> filters = new ArrayList<>();

    if (metricName != null && !metricName.isBlank()) {
      filters.add((root, query, builder) -> builder.equal(root.get("metricName"), metricName));
    }
    if (bucketType != null) {
      filters.add((root, query, builder) -> builder.equal(root.get("bucketType"), bucketType));
    }
    if (dimensionKey != null && !dimensionKey.isBlank()) {
      filters.add((root, query, builder) -> builder.equal(root.get("dimensionKey"), dimensionKey));
    }
    if (dimensionValue != null && !dimensionValue.isBlank()) {
      filters.add(
          (root, query, builder) -> builder.equal(root.get("dimensionValue"), dimensionValue));
    }
    if (from != null) {
      filters.add(
          (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("bucketStart"), from));
    }
    if (to != null) {
      filters.add((root, query, builder) -> builder.lessThan(root.get("bucketStart"), to));
    }

    return Specification.allOf(filters);
  }
}
