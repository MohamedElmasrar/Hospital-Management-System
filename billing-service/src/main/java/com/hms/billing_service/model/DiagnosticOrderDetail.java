package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnostic_order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
