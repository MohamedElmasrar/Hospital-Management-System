package com.hms.billing_service.model;

import com.hms.billing_service.enums.PaymentMethod;
import com.hms.billing_service.enums.PaymentStatus;
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
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
      @Index(name = "idx_payment_reference", columnList = "payment_reference")
    })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invoice_id", nullable = false)
  private Invoice invoice;

  @Column(name = "payment_reference", length = 100, unique = true, nullable = false)
  private String paymentReference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cash_desk_id", nullable = false)
  private CashDesk cashDesk;

  @Column(name = "cashier_id", nullable = false)
  private UUID cashierId;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status = PaymentStatus.COMPLETED;

  @Column(name = "gateway_transaction_id", length = 255, unique = true)
  private String gatewayTransactionId;

  @Column(name = "paid_at", nullable = false)
  private Date paidAt;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}

