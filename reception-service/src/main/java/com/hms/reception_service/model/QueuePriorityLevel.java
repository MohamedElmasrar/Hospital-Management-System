package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "queue_priority_levels")
public class QueuePriorityLevel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "code", unique = true, nullable = false, length = 30)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "weight", nullable = false)
  private Integer weight = 10;

  @Column(name = "badge_color_hex", length = 10)
  private String badgeColorHex = "#10B981";

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;
}
