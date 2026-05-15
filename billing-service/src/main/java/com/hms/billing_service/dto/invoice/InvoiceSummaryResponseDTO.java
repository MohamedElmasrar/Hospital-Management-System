package com.hms.billing_service.dto.invoice;

import com.hms.billing_service.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record InvoiceSummaryResponseDTO(
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
    Date dateCreated,
    Date dateModified) {}

