package com.hms.billing_service.dto.insurance;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record InsurancePolicyResponseDTO(
    UUID id,
    String policyNumber,
    UUID insuranceProviderId,
    String insuranceProviderName,
    String insuranceProviderCode,
    UUID patientId,
    BigDecimal coverageRate,
    Boolean isActive,
    Date dateCreated,
    Date dateModified) {}
