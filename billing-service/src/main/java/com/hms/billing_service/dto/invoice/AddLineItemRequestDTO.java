package com.hms.billing_service.dto.invoice;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AddLineItemRequestDTO(
    @NotNull UUID billableOrderId,
    @NotNull @Min(1) Integer quantity,
    @DecimalMin("0.0000") @DecimalMax("1.0000") BigDecimal discountRate) {}

