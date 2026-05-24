package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "admissions")
public class Admission {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "bed_id", nullable = false, unique = true)
  private Bed bed;

  @Column(name = "reason", nullable = false, length = 500)
  private String reason;

  @Column(name = "admitted_at", nullable = false)
  private LocalDateTime admittedAt;

  @Column(name = "discharged_at")
  private LocalDateTime dischargedAt;
}
