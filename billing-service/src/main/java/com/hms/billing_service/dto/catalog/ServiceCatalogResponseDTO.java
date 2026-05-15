package com.hms.billing_service.dto.catalog;

import java.util.Date;
import java.util.UUID;

public record ServiceCatalogResponseDTO(
    UUID id,
    String serviceCode,
    UUID departmentId,
    String departmentName,
    String name,
    UUID externalServiceId,
    Boolean isActive,
    TariffVersionResponseDTO currentTariff,
    Date dateCreated,
    Date dateModified) {}

