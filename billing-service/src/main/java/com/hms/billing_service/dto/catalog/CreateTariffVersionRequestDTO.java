package com.hms.billing_service.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

public record CreateTariffVersionRequestDTO(
    @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
    @DecimalMin("0.0000") BigDecimal taxRate,
    Date effectiveFrom) {}
