package com.hms.analytics_service.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hms.analytics_service.dto.AnalyticsEventMessage;
import com.hms.analytics_service.enums.EventProcessingStatus;
import com.hms.analytics_service.enums.MetricBucketType;
import com.hms.analytics_service.repository.AnalyticsEventRepository;
import com.hms.analytics_service.repository.MetricAggregateRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AnalyticsEventServiceTest {

  @Autowired private AnalyticsEventService service;
  @Autowired private AnalyticsEventRepository events;
  @Autowired private MetricAggregateRepository metrics;

  @Test
  void ingestPersistsEventAndBucketsExactlyOnce() {
    UUID eventId = UUID.randomUUID();
    Instant occurredAt = Instant.parse("2026-10-30T14:25:00Z");
    AnalyticsEventMessage message =
        new AnalyticsEventMessage(
            eventId,
            "invoice.paid",
            "billing-service",
            "invoice",
            UUID.randomUUID().toString(),
            1,
            occurredAt,
            "{}",
            "revenue",
            new BigDecimal("125.50"),
            "payment_method",
            "CARD");

    service.ingest(message);
    service.ingest(message);

    var event = events.findAll().getFirst();
    assertThat(events.count()).isEqualTo(1);
    assertThat(event.getProcessingStatus()).isEqualTo(EventProcessingStatus.PROCESSED);
    assertThat(event.getEventId()).isEqualTo(eventId);
    assertThat(metrics.count()).isEqualTo(3);
    var hourly =
        metrics
            .findByMetricNameAndBucketTypeAndBucketStartAndDimensionKeyAndDimensionValue(
                "revenue",
                MetricBucketType.HOUR,
                Instant.parse("2026-10-30T14:00:00Z"),
                "payment_method",
                "CARD")
            .orElseThrow();
    assertThat(hourly.getEventCount()).isEqualTo(1);
    assertThat(hourly.getNumericTotal()).isEqualByComparingTo("125.50");
  }
}
