package com.hms.billing_service.messaging;

import com.hms.billing_service.dto.notification.NotificationEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.*;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper mapper;

  @Value("${notification.kafka.topic:hms.notifications.events}")
  private String topic;

  public void publish(UUID recipientId, String title, String message) {
    if (recipientId == null) return;
    var event =
        new NotificationEvent(
            UUID.randomUUID(), recipientId, "billing-service", title, message, Instant.now());
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(event);
            }
          });
    } else send(event);
  }

  private void send(NotificationEvent event) {
    try {
      kafkaTemplate
          .send(topic, event.recipientId().toString(), mapper.writeValueAsString(event))
          .whenComplete(
              (result, error) -> {
                if (error != null)
                  log.warn("Could not publish notification {}", event.eventId(), error);
              });
    } catch (Exception error) {
      log.warn("Could not publish notification {}", event.eventId(), error);
    }
  }
}
