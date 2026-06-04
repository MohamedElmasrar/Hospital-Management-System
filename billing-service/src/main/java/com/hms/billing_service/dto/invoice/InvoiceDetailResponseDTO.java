package com.hms.billing_service.dto.invoice;

import com.hms.billing_service.dto.insurance.InsuranceClaimResponseDTO;
import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InvoiceDetailResponseDTO(
    UUID id,
    String invoiceNumber,
    UUID patientId,
    UUID cashDeskId,
    String cashDeskCode,
    UUID issuedByStaffId,
    InvoiceStatus status,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal totalTax,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal balanceDue,
    Date issuedAt,
    Date dueDate,
    List<InvoiceLineItemResponseDTO> lineItems,
    List<PaymentResponseDTO> payments,
    InsuranceClaimResponseDTO insuranceClaim,
    Date dateCreated,
    Date dateModified) {}
