package com.hms.notification_service.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "notification_recipient_created", columnList = "recipientId,createdAt"),
      @Index(name = "notification_recipient_read", columnList = "recipientId,readAt")
    })
public class Notification {
  @Id private UUID id;

  @Column(nullable = false)
  private UUID recipientId;

  @Column(nullable = false, length = 100)
  private String sourceService;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant readAt;
}
