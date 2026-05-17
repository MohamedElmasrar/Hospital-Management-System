package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "insurance_providers")
@EntityListeners(AuditingEntityListener.class)
@Data
public class InsuranceProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "name", length = 150, unique = true, nullable = false)
  private String name;

  @Column(name = "code", length = 50, unique = true, nullable = false)
  private String code;

  @Column(name = "contact_email", length = 100)
  private String contactEmail;

  @Column(name = "contact_phone", length = 50)
  private String contactPhone;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}
