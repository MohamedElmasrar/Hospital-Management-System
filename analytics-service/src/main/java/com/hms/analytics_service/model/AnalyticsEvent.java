package com.hms.analytics_service.model;

import com.hms.analytics_service.enums.EventProcessingStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(
    name = "analytics_events",
    uniqueConstraints = @UniqueConstraint(name = "uk_analytics_event_id", columnNames = "event_id"),
    indexes = {
      @Index(name = "idx_analytics_event_type_time", columnList = "event_type, occurred_at"),
      @Index(name = "idx_analytics_event_source_time", columnList = "source_service, occurred_at"),
      @Index(name = "idx_analytics_event_aggregate", columnList = "aggregate_type, aggregate_id"),
      @Index(name = "idx_analytics_event_status", columnList = "processing_status")
    })
@EntityListeners(AuditingEntityListener.class)
public class AnalyticsEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @Column(name = "source_service", nullable = false, length = 60)
  private String sourceService;

  @Column(name = "aggregate_type", length = 80)
  private String aggregateType;

  @Column(name = "aggregate_id", length = 100)
  private String aggregateId;

  @Column(name = "schema_version", nullable = false)
  private Integer schemaVersion = 1;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @CreatedDate
  @Column(name = "received_at", nullable = false, updatable = false)
  private Instant receivedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status", nullable = false, length = 20)
  private EventProcessingStatus processingStatus = EventProcessingStatus.RECEIVED;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "processing_error", columnDefinition = "TEXT")
  private String processingError;
}
