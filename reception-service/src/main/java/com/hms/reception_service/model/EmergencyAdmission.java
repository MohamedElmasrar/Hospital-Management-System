package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "emergency_admissions")
public class EmergencyAdmission {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id")
  private Patient patient;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "chief_complaint", nullable = false, length = 500)
  private String chiefComplaint;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "closed_at")
  private LocalDateTime closedAt;
}
