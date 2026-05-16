package com.hms.billing_service.dto.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record CreateServiceCatalogRequestDTO(
    @NotBlank @Length(max = 50) String serviceCode,
    @NotNull UUID departmentId,
    @NotBlank @Length(max = 255) String name,
    UUID externalServiceId,
    @NotNull @DecimalMin("0.00") BigDecimal initialUnitPrice,
    @DecimalMin("0.0000") BigDecimal initialTaxRate) {}
