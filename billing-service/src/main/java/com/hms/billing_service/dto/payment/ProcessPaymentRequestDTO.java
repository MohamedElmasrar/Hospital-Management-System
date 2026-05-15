package com.hms.billing_service.dto.payment;

import com.hms.billing_service.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record ProcessPaymentRequestDTO(
    @NotNull UUID invoiceId,
    @NotNull UUID cashDeskId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotNull PaymentMethod method,
    @Length(max = 255) String gatewayTransactionId) {}

