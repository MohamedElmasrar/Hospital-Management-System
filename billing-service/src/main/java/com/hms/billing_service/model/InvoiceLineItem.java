package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "invoice_line_items",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_invoice_line_item_billable_order",
          columnNames = {"billable_order_id"})
    })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItem {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "billable_order_id", nullable = false, unique = true)
  private BillableOrder billableOrder;

  @Column(name = "quantity", nullable = false)
  private Integer quantity = 1;

  @Column(name = "discount_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal discountRate = BigDecimal.ZERO;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}
