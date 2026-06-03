package com.hms.auth_service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {
  @Mock KafkaTemplate<String, String> kafka;

  @AfterEach
  void clear() {
    if (TransactionSynchronizationManager.isSynchronizationActive())
      TransactionSynchronizationManager.clearSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(false);
  }

  @Test
  void sendsToUserOnlyAfterCommit() {
    var publisher = new NotificationEventPublisher(kafka, new ObjectMapper());
    ReflectionTestUtils.setField(publisher, "topic", "hms.notifications.events");
    UUID user = UUID.randomUUID();
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);
    publisher.publish(user, "Shift assigned", "Check your schedule.");
    verifyNoInteractions(kafka);
    when(kafka.send(anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(null));
    TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
    var payload = ArgumentCaptor.forClass(String.class);
    verify(kafka).send(eq("hms.notifications.events"), eq(user.toString()), payload.capture());
    assertThat(payload.getValue())
        .contains("\"recipientId\":\"" + user + "\"")
        .contains("auth-service")
        .contains("Shift assigned");
  }

  @Test
  void skipsUnlinkedEmployees() {
    new NotificationEventPublisher(kafka, new ObjectMapper())
        .publish(null, "Shift assigned", "Check schedule.");
    verifyNoInteractions(kafka);
  }

  @Test
  void rollbackDoesNotSend() {
    TransactionSynchronizationManager.initSynchronization();
    TransactionSynchronizationManager.setActualTransactionActive(true);
    new NotificationEventPublisher(kafka, new ObjectMapper())
        .publish(UUID.randomUUID(), "Title", "Message");
    TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(1));
    verifyNoInteractions(kafka);
  }
}
