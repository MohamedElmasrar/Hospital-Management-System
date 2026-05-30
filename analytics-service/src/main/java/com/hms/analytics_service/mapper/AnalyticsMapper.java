package com.hms.analytics_service.mapper;

import com.hms.analytics_service.dto.AnalyticsEventMessage;
import com.hms.analytics_service.dto.AnalyticsEventResponse;
import com.hms.analytics_service.dto.MetricAggregateResponse;
import com.hms.analytics_service.model.AnalyticsEvent;
import com.hms.analytics_service.model.MetricAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "receivedAt", ignore = true)
  @Mapping(target = "processingStatus", ignore = true)
  @Mapping(target = "processingError", ignore = true)
  AnalyticsEvent toEntity(AnalyticsEventMessage message);

  AnalyticsEventResponse toResponse(AnalyticsEvent event);

  MetricAggregateResponse toResponse(MetricAggregate aggregate);
}
