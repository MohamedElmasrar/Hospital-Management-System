package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "diagnostic_order_details")
@Data
public class DiagnosticOrderDetail {

  @Id
  @Column(name = "billable_order_id")
  private UUID billableOrderId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "billable_order_id")
  private BillableOrder billableOrder;

  @Column(name = "modality", length = 50, nullable = false)
  private String modality;

  @Column(name = "body_part", length = 100, nullable = false)
  private String bodyPart;

  @Column(name = "contrast_used", nullable = false)
  private Boolean contrastUsed = false;
}
