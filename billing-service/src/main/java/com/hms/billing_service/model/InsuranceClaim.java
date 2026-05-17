package com.hms.billing_service.model;

import com.hms.billing_service.enums.ClaimStatus;
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
    name = "insurance_claims",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_insurance_claim_invoice",
          columnNames = {"invoice_id"})
    },
    indexes = {@Index(name = "idx_insurance_claim_policy", columnList = "policy_id")})
@EntityListeners(AuditingEntityListener.class)
@Data
public class InsuranceClaim {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false, unique = true)
  private Invoice invoice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id", nullable = false)
  private PatientInsurancePolicy policy;

  @Column(name = "claimed_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal claimedAmount;

  @Column(name = "approved_amount", precision = 12, scale = 2)
  private BigDecimal approvedAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ClaimStatus status = ClaimStatus.SUBMITTED;

  @Column(name = "submitted_at", nullable = false)
  private Date submittedAt;

  @Column(name = "settled_at")
  private Date settledAt;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}
