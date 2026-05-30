package com.hms.analytics_service.model;

import com.hms.analytics_service.enums.MetricBucketType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(
    name = "metric_aggregates",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_metric_bucket_dimension",
            columnNames = {
              "metric_name",
              "bucket_type",
              "bucket_start",
              "dimension_key",
              "dimension_value"
            }),
    indexes = {
      @Index(name = "idx_metric_name_bucket", columnList = "metric_name, bucket_start"),
      @Index(name = "idx_metric_dimension", columnList = "dimension_key, dimension_value")
    })
@EntityListeners(AuditingEntityListener.class)
public class MetricAggregate {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "metric_name", nullable = false, length = 120)
  private String metricName;

  @Enumerated(EnumType.STRING)
  @Column(name = "bucket_type", nullable = false, length = 20)
  private MetricBucketType bucketType;

  @Column(name = "bucket_start", nullable = false)
  private Instant bucketStart;

  @Column(name = "bucket_end", nullable = false)
  private Instant bucketEnd;

  @Column(name = "dimension_key", nullable = false, length = 80)
  private String dimensionKey = "all";

  @Column(name = "dimension_value", nullable = false, length = 160)
  private String dimensionValue = "all";

  @Column(name = "event_count", nullable = false)
  private Long eventCount = 0L;

  @Column(name = "numeric_total", nullable = false, precision = 19, scale = 4)
  private BigDecimal numericTotal = BigDecimal.ZERO;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
