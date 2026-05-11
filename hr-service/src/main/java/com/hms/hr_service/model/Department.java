package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "department",
    indexes = {
      @Index(name = "idx_medical_organization_id", columnList = "medical_organization_id")
    })
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "medical_organization_id", nullable = false)
  private MedicalOrganization medicalOrganization;

  @ManyToOne
  @JoinColumn(name = "parent_department_id")
  private Department parentDepartment;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private Date dateCreated;
}
