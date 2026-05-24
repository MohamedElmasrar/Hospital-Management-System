package com.hms.billing_service.model;

import com.hms.billing_service.enums.SourceOrderType;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "billable_orders",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_billable_order_external_order",
          columnNames = {"external_order_id", "order_type"})
    },
    indexes = {
      @Index(name = "idx_billable_order_patient", columnList = "patient_id"),
      @Index(name = "idx_billable_order_practitioner", columnList = "practitioner_id")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
public class BillableOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "external_order_id", nullable = false)
  private UUID externalOrderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_type", nullable = false)
  private SourceOrderType orderType;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @Column(name = "practitioner_id", nullable = false)
  private UUID practitionerId;

  @Column(name = "department_id")
  private UUID departmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tariff_version_id", nullable = false)
  private ServiceTariffVersion tariffVersion;

  @Column(name = "performed_at", nullable = false)
  private Date performedAt;

  @OneToOne(mappedBy = "billableOrder", cascade = CascadeType.ALL, orphanRemoval = true)
  private DiagnosticOrderDetail diagnosticOrderDetail;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  public void setDiagnosticOrderDetail(DiagnosticOrderDetail detail) {
    this.diagnosticOrderDetail = detail;
    if (detail != null) {
      detail.setBillableOrder(this);
    }
  }
}
