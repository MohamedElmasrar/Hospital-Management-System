package com.hms.billing_service.dto.insurance;

import com.hms.billing_service.enums.ClaimStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record InsuranceClaimResponseDTO(
    UUID id,
    UUID invoiceId,
    String invoiceNumber,
    UUID policyId,
    String policyNumber,
    String insuranceProviderName,
    BigDecimal claimedAmount,
    BigDecimal approvedAmount,
    ClaimStatus status,
    Date submittedAt,
    Date settledAt,
    Date dateCreated,
    Date dateModified) {}
