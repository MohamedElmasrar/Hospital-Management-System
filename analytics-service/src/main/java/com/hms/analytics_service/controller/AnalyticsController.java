package com.hms.analytics_service.controller;

import com.hms.analytics_service.dto.AnalyticsEventResponse;
import com.hms.analytics_service.dto.MetricAggregateResponse;
import com.hms.analytics_service.dto.PageResponse;
import com.hms.analytics_service.enums.MetricBucketType;
import com.hms.analytics_service.service.AnalyticsEventService;
import com.hms.analytics_service.service.MetricAggregateService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsEventService eventService;
  private final MetricAggregateService metricService;

  @GetMapping("/events")
  public ResponseEntity<PageResponse<AnalyticsEventResponse>> getEvents(
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String sourceService,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var pageable = PageRequest.of(page, size, Sort.by("occurredAt").descending());
    return ResponseEntity.ok(eventService.search(eventType, sourceService, from, to, pageable));
  }

  @GetMapping("/metrics")
  public ResponseEntity<PageResponse<MetricAggregateResponse>> getMetrics(
      @RequestParam(required = false) String metricName,
      @RequestParam(required = false) MetricBucketType bucketType,
      @RequestParam(required = false) String dimensionKey,
      @RequestParam(required = false) String dimensionValue,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    var pageable =
        PageRequest.of(
            page, size, Sort.by("bucketStart").descending().and(Sort.by("metricName").ascending()));
    return ResponseEntity.ok(
        metricService.search(
            metricName, bucketType, dimensionKey, dimensionValue, from, to, pageable));
  }
}
