package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "appointment_reminders")
public class AppointmentReminder {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reservation_id", nullable = false)
  private Reservation reservation;

  @Column(name = "channel", nullable = false, length = 20)
  private String channel;

  @Column(name = "recipient", nullable = false, length = 150)
  private String recipient;

  @Column(name = "scheduled_for", nullable = false)
  private LocalDateTime scheduledFor;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;
}
