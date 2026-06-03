package com.hms.notification_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hms.notification_service.dto.NotificationEvent;
import com.hms.notification_service.messaging.NotificationListener;
import com.hms.notification_service.repository.NotificationRepository;
import com.hms.notification_service.service.NotificationService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationServiceApplicationTests {
  @Autowired NotificationRepository repository;
  @Autowired NotificationService service;
  @Autowired NotificationListener listener;
  @Autowired ObjectMapper mapper;
  @Autowired MockMvc mvc;

  @BeforeEach
  void clear() {
    repository.deleteAll();
  }

  private NotificationEvent event(UUID user) {
    return new NotificationEvent(
        UUID.randomUUID(),
        user,
        "hr-service",
        "Shift assigned",
        "Check your schedule.",
        Instant.now());
  }

  @Test
  void kafkaRedeliveryDoesNotDuplicateNotificationsAndMappingPreservesFields() {
    var message = event(UUID.randomUUID());
    String payload = mapper.writeValueAsString(message);
    listener.consume(payload);
    listener.consume(payload);
    assertThat(repository.count()).isEqualTo(1);
    var stored = repository.findById(message.eventId()).orElseThrow();
    assertThat(stored.getRecipientId()).isEqualTo(message.recipientId());
    assertThat(stored.getTitle()).isEqualTo(message.title());
    assertThat(stored.getMessage()).isEqualTo(message.message());
    assertThat(stored.getSourceService()).isEqualTo(message.sourceService());
    assertThat(stored.getCreatedAt())
        .isCloseTo(
            message.occurredAt(),
            org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MICROS));
    assertThat(stored.getReadAt()).isNull();
  }

  @Test
  void inboxAndReadActionsAreScopedToAuthenticatedUser() throws Exception {
    UUID owner = UUID.randomUUID(), other = UUID.randomUUID();
    var own = event(owner);
    var foreign = event(other);
    service.ingest(own);
    service.ingest(foreign);
    var auth = jwt().jwt(j -> j.subject(owner.toString()));
    mvc.perform(get("/api/notifications").with(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].id").value(own.eventId().toString()));
    mvc.perform(patch("/api/notifications/" + foreign.eventId() + "/read").with(auth))
        .andExpect(status().isNotFound());
    mvc.perform(patch("/api/notifications/" + own.eventId() + "/read").with(auth))
        .andExpect(status().isNoContent());
    Instant readAt = repository.findById(own.eventId()).orElseThrow().getReadAt();
    mvc.perform(patch("/api/notifications/" + own.eventId() + "/read").with(auth))
        .andExpect(status().isNoContent());
    assertThat(repository.findById(own.eventId()).orElseThrow().getReadAt()).isEqualTo(readAt);
    mvc.perform(get("/api/notifications/unread-count").with(auth))
        .andExpect(jsonPath("$.count").value(0));
    assertThat(repository.findById(foreign.eventId()).orElseThrow().getReadAt()).isNull();
  }

  @Test
  void markAllReadDoesNotAffectOtherUsers() throws Exception {
    UUID owner = UUID.randomUUID();
    var own = event(owner);
    var other = event(UUID.randomUUID());
    service.ingest(own);
    service.ingest(other);
    mvc.perform(
            patch("/api/notifications/read-all").with(jwt().jwt(j -> j.subject(owner.toString()))))
        .andExpect(status().isNoContent());
    assertThat(repository.findById(own.eventId()).orElseThrow().getReadAt()).isNotNull();
    assertThat(repository.findById(other.eventId()).orElseThrow().getReadAt()).isNull();
  }

  @Test
  void anonymousRequestsAreRejected() throws Exception {
    mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
  }
}
