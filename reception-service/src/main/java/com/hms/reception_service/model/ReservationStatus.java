package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "reservation_statuses")
public class ReservationStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "code", unique = true, nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "badge_color_hex", length = 10)
  private String badgeColorHex = "#6B7280";

  @Column(name = "allows_queueing", nullable = false)
  private Boolean allowsQueueing = Boolean.FALSE;

  @Column(name = "is_terminal", nullable = false)
  private Boolean isTerminal = Boolean.FALSE;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;
}
