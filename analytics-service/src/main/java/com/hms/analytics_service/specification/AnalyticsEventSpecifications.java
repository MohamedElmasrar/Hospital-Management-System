package com.hms.analytics_service.specification;

import com.hms.analytics_service.model.AnalyticsEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AnalyticsEventSpecifications {

  private AnalyticsEventSpecifications() {}

  public static Specification<AnalyticsEvent> search(
      String eventType, String sourceService, Instant from, Instant to) {
    List<Specification<AnalyticsEvent>> filters = new ArrayList<>();

    if (eventType != null && !eventType.isBlank()) {
      filters.add(
          (root, query, builder) ->
              builder.equal(builder.lower(root.get("eventType")), eventType.toLowerCase()));
    }
    if (sourceService != null && !sourceService.isBlank()) {
      filters.add(
          (root, query, builder) ->
              builder.equal(builder.lower(root.get("sourceService")), sourceService.toLowerCase()));
    }
    if (from != null) {
      filters.add(
          (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("occurredAt"), from));
    }
    if (to != null) {
      filters.add((root, query, builder) -> builder.lessThanOrEqualTo(root.get("occurredAt"), to));
    }

    return Specification.allOf(filters);
  }
}
