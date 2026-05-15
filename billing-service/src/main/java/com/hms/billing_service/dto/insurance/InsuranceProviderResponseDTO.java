package com.hms.billing_service.dto.insurance;

import java.util.Date;
import java.util.UUID;

public record InsuranceProviderResponseDTO(
    UUID id,
    String name,
    String code,
    String contactEmail,
    String contactPhone,
    Boolean isActive,
    Date dateCreated,
    Date dateModified) {}

