package com.hms.analytics_service.service;

import com.hms.analytics_service.dto.AnalyticsEventMessage;
import com.hms.analytics_service.dto.AnalyticsEventResponse;
import com.hms.analytics_service.dto.PageResponse;
import com.hms.analytics_service.enums.EventProcessingStatus;
import com.hms.analytics_service.mapper.AnalyticsMapper;
import com.hms.analytics_service.model.AnalyticsEvent;
import com.hms.analytics_service.repository.AnalyticsEventRepository;
import com.hms.analytics_service.specification.AnalyticsEventSpecifications;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsEventService {

  private final AnalyticsEventRepository repository;
  private final MetricAggregateService metricAggregateService;
  private final AnalyticsMapper mapper;

  @Transactional
  public void ingest(AnalyticsEventMessage message) {
    if (repository.existsByEventId(message.eventId())) {
      return;
    }

    AnalyticsEvent event = mapper.toEntity(message);
    if (event.getSchemaVersion() == null) {
      event.setSchemaVersion(1);
    }
    event.setProcessingStatus(EventProcessingStatus.RECEIVED);
    event = repository.save(event);

    metricAggregateService.record(message);

    event.setProcessingStatus(EventProcessingStatus.PROCESSED);
    repository.save(event);
  }

  @Transactional(readOnly = true)
  public PageResponse<AnalyticsEventResponse> search(
      String eventType, String sourceService, Instant from, Instant to, Pageable pageable) {
    return PageResponse.from(
        repository
            .findAll(
                AnalyticsEventSpecifications.search(eventType, sourceService, from, to), pageable)
            .map(mapper::toResponse));
  }
}
