package com.hms.billing_service.dto.report;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record UnpaidInvoiceResponseDTO(
    UUID invoiceId,
    String invoiceNumber,
    UUID patientId,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal balanceDue,
    Date dueDate) {}
