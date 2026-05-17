package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "diagnostic_modalities")
@EntityListeners(AuditingEntityListener.class)
public class DiagnosticModality {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "code", unique = true, nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @Column(name = "default_duration_minutes", nullable = false)
  private Integer defaultDurationMinutes = 30;

  @Column(name = "requires_contrast_screening", nullable = false)
  private Boolean requiresContrastScreening = Boolean.FALSE;

  @Column(name = "requires_safety_checklist", nullable = false)
  private Boolean requiresSafetyChecklist = Boolean.FALSE;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}
