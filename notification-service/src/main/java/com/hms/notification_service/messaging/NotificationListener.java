package com.hms.notification_service.messaging;

import com.hms.notification_service.dto.NotificationEvent;
import com.hms.notification_service.service.NotificationService;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class NotificationListener {
  private final ObjectMapper mapper;
  private final Validator validator;
  private final NotificationService service;

  @KafkaListener(topics = "${notification.kafka.topic}")
  public void consume(String payload) {
    var event = mapper.readValue(payload, NotificationEvent.class);
    var errors = validator.validate(event);
    if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
    service.ingest(event);
  }
}
