package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "patient_insurance_policies",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_policy_provider_number",
          columnNames = {"insurance_provider_id", "policy_number"})
    },
    indexes = {@Index(name = "idx_policy_patient", columnList = "patient_id")})
@EntityListeners(AuditingEntityListener.class)
@Data
public class PatientInsurancePolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "policy_number", length = 100, nullable = false)
  private String policyNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insurance_provider_id", nullable = false)
  private InsuranceProvider insuranceProvider;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @Column(name = "coverage_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal coverageRate;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}
