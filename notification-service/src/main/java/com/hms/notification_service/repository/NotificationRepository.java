package com.hms.notification_service.repository;

import com.hms.notification_service.model.Notification;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

  Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

  long countByRecipientIdAndReadAtIsNull(UUID recipientId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "update Notification n set n.readAt = :now where n.recipientId = :recipientId and n.readAt is null")
  int markAllRead(@Param("recipientId") UUID recipientId, @Param("now") Instant now);
}
