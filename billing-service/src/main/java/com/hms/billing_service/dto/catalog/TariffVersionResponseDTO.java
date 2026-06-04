package com.hms.billing_service.dto.catalog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TariffVersionResponseDTO(
    UUID id,
    UUID catalogItemId,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    Date effectiveFrom,
    Date effectiveTo,
    Date dateCreated) {}
