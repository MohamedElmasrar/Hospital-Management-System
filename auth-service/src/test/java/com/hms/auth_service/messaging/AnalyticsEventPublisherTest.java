package com.hms.auth_service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hms.auth_service.enums.UserRole;
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
  void publishesAnalyticsEnvelopeWithoutPersonalDetails() {
    AnalyticsEventPublisher publisher =
        new AnalyticsEventPublisher(kafkaTemplate, new ObjectMapper());
    ReflectionTestUtils.setField(publisher, "topic", "hms.analytics.events");
    UUID userId = UUID.randomUUID();
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));

    publisher.publishUserEvent("USER_REGISTERED", userId, UserRole.EMPLOYEE);

    ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate)
        .send(
            org.mockito.Mockito.eq("hms.analytics.events"),
            org.mockito.Mockito.eq(userId.toString()),
            payload.capture());
    assertThat(payload.getValue())
        .contains("\"eventType\":\"USER_REGISTERED\"")
        .contains("\"sourceService\":\"auth-service\"")
        .contains("\"aggregateId\":\"" + userId + "\"")
        .contains("\"metricName\":\"USER_REGISTERED\"")
        .contains("\"dimensionValue\":\"EMPLOYEE\"")
        .doesNotContain("email", "password", "token");
  }

  @Test
  void waitsForTransactionCommit() {
    AnalyticsEventPublisher publisher =
        new AnalyticsEventPublisher(kafkaTemplate, new ObjectMapper());
    ReflectionTestUtils.setField(publisher, "topic", "hms.analytics.events");
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);
    when(kafkaTemplate.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));

    publisher.publishUserEvent("USER_REGISTERED", UUID.randomUUID(), UserRole.DOCTOR);
    verifyNoInteractions(kafkaTemplate);

    TransactionSynchronizationManager.getSynchronizations()
        .forEach(synchronization -> synchronization.afterCommit());
    verify(kafkaTemplate).send(anyString(), anyString(), anyString());
  }
}
