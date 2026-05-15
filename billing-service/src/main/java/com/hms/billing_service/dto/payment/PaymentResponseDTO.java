package com.hms.billing_service.dto.payment;

import com.hms.billing_service.enums.PaymentMethod;
import com.hms.billing_service.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record PaymentResponseDTO(
    UUID id,
    UUID invoiceId,
    String invoiceNumber,
    String paymentReference,
    UUID cashDeskId,
    String cashDeskCode,
    UUID cashierId,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    String gatewayTransactionId,
    Date paidAt,
    Date dateCreated) {}

