package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(
    name = "medical_organization",
    indexes = {
      @Index(name = "idx_identifier", columnList = "identifier"),
      @Index(name = "idx_name", columnList = "name")
    })
@EntityListeners(AuditingEntityListener.class)
public class MedicalOrganization {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "legal_name")
  private String legalName;

  @Column(name = "identifier", unique = true, nullable = false)
  private String identifier;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false, updatable = false)
  private LocalDateTime updatedAt;
}
