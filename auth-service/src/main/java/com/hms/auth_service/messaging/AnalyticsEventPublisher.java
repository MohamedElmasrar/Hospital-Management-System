package com.hms.auth_service.messaging;

import com.hms.auth_service.enums.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventPublisher {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${analytics.kafka.topic}")
  private String topic;

  public void publishUserEvent(String eventType, UUID userId, UserRole role) {
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(eventType, userId, role);
            }
          });
    } else {
      send(eventType, userId, role);
    }
  }

  private void send(String eventType, UUID userId, UserRole role) {
    try {
      String payload =
          objectMapper.writeValueAsString(
              EventMessage.builder()
                  .eventId(UUID.randomUUID())
                  .eventType(eventType)
                  .sourceService("auth-service")
                  .aggregateType("USER")
                  .aggregateId(userId.toString())
                  .schemaVersion(1)
                  .occurredAt(Instant.now())
                  .payload("{}")
                  .metricName(eventType)
                  .dimensionKey("role")
                  .dimensionValue(role.name())
                  .build());
      kafkaTemplate
          .send(topic, userId.toString(), payload)
          .whenComplete(
              (result, error) -> {
                if (error != null) {
                  log.warn(
                      "Could not publish analytics event {} for user {}", eventType, userId, error);
                }
              });
    } catch (Exception error) {
      log.warn("Could not publish analytics event {} for user {}", eventType, userId, error);
    }
  }

  @Builder
  private record EventMessage(
      UUID eventId,
      String eventType,
      String sourceService,
      String aggregateType,
      String aggregateId,
      Integer schemaVersion,
      Instant occurredAt,
      String payload,
      String metricName,
      BigDecimal numericValue,
      String dimensionKey,
      String dimensionValue) {}
}
