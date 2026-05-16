package com.hms.billing_service.dto.catalog;

import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record UpdateServiceCatalogRequestDTO(
    @Length(max = 50) String serviceCode,
    UUID departmentId,
    @Length(max = 255) String name,
    UUID externalServiceId,
    Boolean isActive) {}
