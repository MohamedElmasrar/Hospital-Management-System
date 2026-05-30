package com.hms.hr_service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventPublisherTest {
  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  @AfterEach
  void clearTransaction() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
    TransactionSynchronizationManager.setActualTransactionActive(false);
  }

  @Test
  void publishesTheAnalyticsEnvelopeWithoutPersonalDetails() {
    AnalyticsEventPublisher publisher = publisher();
    UUID aggregateId = UUID.randomUUID();
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));

    publisher.publish("DEPARTMENT_CREATED", "DEPARTMENT", aggregateId);

    ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate)
        .send(
            org.mockito.Mockito.eq("hms.analytics.events"),
            org.mockito.Mockito.eq(aggregateId.toString()),
            payload.capture());
    assertThat(payload.getValue())
        .contains("\"eventType\":\"DEPARTMENT_CREATED\"")
        .contains("\"sourceService\":\"hr-service\"")
        .contains("\"aggregateId\":\"" + aggregateId + "\"")
        .contains("\"metricName\":\"DEPARTMENT_CREATED\"")
        .doesNotContain("email", "password", "patientName");
  }

  @Test
  void sendsOnlyAfterCommit() {
    AnalyticsEventPublisher publisher = publisher();
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));

    publisher.publish("PATIENT_CREATED", "PATIENT", UUID.randomUUID());
    verifyNoInteractions(kafkaTemplate);

    TransactionSynchronizationManager.getSynchronizations()
        .forEach(synchronization -> synchronization.afterCommit());
    verify(kafkaTemplate).send(anyString(), anyString(), anyString());
  }

  @Test
  void rolledBackTransactionDoesNotSend() {
    AnalyticsEventPublisher publisher = publisher();
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);

    publisher.publish("PATIENT_CREATED", "PATIENT", UUID.randomUUID());
    TransactionSynchronizationManager.getSynchronizations()
        .forEach(synchronization -> synchronization.afterCompletion(1));

    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  void failedKafkaSendDoesNotFailTheBusinessOperation() {
    AnalyticsEventPublisher publisher = publisher();
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker unavailable")));

    publisher.publish("PATIENT_CREATED", "PATIENT", UUID.randomUUID());

    verify(kafkaTemplate).send(anyString(), anyString(), anyString());
  }

  private AnalyticsEventPublisher publisher() {
    AnalyticsEventPublisher publisher =
        new AnalyticsEventPublisher(kafkaTemplate, new ObjectMapper());
    ReflectionTestUtils.setField(publisher, "topic", "hms.analytics.events");
    return publisher;
  }
}
