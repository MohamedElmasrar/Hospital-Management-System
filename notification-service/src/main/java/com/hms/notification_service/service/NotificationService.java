package com.hms.notification_service.service;

import com.hms.notification_service.dto.*;
import com.hms.notification_service.mapper.NotificationMapper;
import com.hms.notification_service.repository.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository repository;
  private final NotificationMapper mapper;

  @Transactional
  public void ingest(NotificationEvent event) {
    if (!repository.existsById(event.eventId())) repository.save(mapper.toEntity(event));
  }

  @Transactional(readOnly = true)
  public PageResponse<NotificationResponse> list(UUID userId, Pageable pageable) {
    return PageResponse.from(
        repository.findByRecipientId(userId, pageable).map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public UnreadCountResponse unreadCount(UUID userId) {
    return new UnreadCountResponse(repository.countByRecipientIdAndReadAtIsNull(userId));
  }

  @Transactional
  public void markRead(UUID id, UUID userId) {
    var notification =
        repository
            .findByIdAndRecipientId(id, userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    if (notification.getReadAt() == null) notification.setReadAt(Instant.now());
  }

  @Transactional
  public void markAllRead(UUID userId) {
    repository.markAllRead(userId, Instant.now());
  }
}
