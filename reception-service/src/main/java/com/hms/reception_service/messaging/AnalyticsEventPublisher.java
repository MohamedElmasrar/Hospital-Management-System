package com.hms.reception_service.messaging;

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

  public void publish(String eventType, String aggregateType, UUID aggregateId) {
    publish(eventType, aggregateType, aggregateId, null, null, null);
  }

  public void publish(
      String eventType,
      String aggregateType,
      UUID aggregateId,
      String dimensionKey,
      String dimensionValue) {
    publish(eventType, aggregateType, aggregateId, null, dimensionKey, dimensionValue);
  }

  public void publish(
      String eventType,
      String aggregateType,
      UUID aggregateId,
      BigDecimal numericValue,
      String dimensionKey,
      String dimensionValue) {
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              send(
                  eventType,
                  aggregateType,
                  aggregateId,
                  numericValue,
                  dimensionKey,
                  dimensionValue);
            }
          });
    } else {
      send(eventType, aggregateType, aggregateId, numericValue, dimensionKey, dimensionValue);
    }
  }

  private void send(
      String eventType,
      String aggregateType,
      UUID aggregateId,
      BigDecimal numericValue,
      String dimensionKey,
      String dimensionValue) {
    try {
      String payload =
          objectMapper.writeValueAsString(
              EventMessage.builder()
                  .eventId(UUID.randomUUID())
                  .eventType(eventType)
                  .sourceService("reception-service")
                  .aggregateType(aggregateType)
                  .aggregateId(aggregateId.toString())
                  .schemaVersion(1)
                  .occurredAt(Instant.now())
                  .payload("{}")
                  .metricName(eventType)
                  .numericValue(numericValue)
                  .dimensionKey(dimensionKey)
                  .dimensionValue(dimensionValue)
                  .build());
      kafkaTemplate
          .send(topic, aggregateId.toString(), payload)
          .whenComplete(
              (result, error) -> {
                if (error != null) {
                  log.warn(
                      "Could not publish analytics event {} for {}", eventType, aggregateId, error);
                }
              });
    } catch (Exception error) {
      log.warn("Could not publish analytics event {} for {}", eventType, aggregateId, error);
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
