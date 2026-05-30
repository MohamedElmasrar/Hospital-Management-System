package com.hms.analytics_service.messaging;

import com.hms.analytics_service.dto.AnalyticsEventMessage;
import com.hms.analytics_service.service.AnalyticsEventService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final AnalyticsEventService eventService;

  @KafkaListener(topics = "${analytics.kafka.topic}")
  public void consume(String payload) throws JacksonException {
    AnalyticsEventMessage message = objectMapper.readValue(payload, AnalyticsEventMessage.class);
    var violations = validator.validate(message);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    eventService.ingest(message);
    log.debug("Processed analytics event {} ({})", message.eventId(), message.eventType());
  }
}
