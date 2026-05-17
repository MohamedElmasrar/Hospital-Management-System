package com.hms.billing_service.model;

import com.hms.billing_service.enums.InvoiceStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "invoices",
    indexes = {
      @Index(name = "idx_invoice_patient", columnList = "patient_id"),
      @Index(name = "idx_invoice_status", columnList = "status"),
      @Index(name = "idx_invoice_cash_desk", columnList = "cash_desk_id")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "invoice_number", length = 30, unique = true, nullable = false)
  private String invoiceNumber;

  @Column(name = "patient_id", nullable = false)
  private UUID patientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cash_desk_id", nullable = false)
  private CashDesk cashDesk;

  @Column(name = "issued_by_staff_id", nullable = false)
  private UUID issuedByStaffId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @Column(name = "issued_at", nullable = false)
  private Date issuedAt;

  @Column(name = "due_date")
  private Date dueDate;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvoiceLineItem> lineItems = new ArrayList<>();

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
  private List<Payment> payments = new ArrayList<>();

  @OneToOne(mappedBy = "invoice")
  private InsuranceClaim insuranceClaim;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;

  public void addLineItem(InvoiceLineItem item) {
    lineItems.add(item);
    item.setInvoice(this);
  }

  public void removeLineItem(InvoiceLineItem item) {
    lineItems.remove(item);
    item.setInvoice(null);
  }
}
