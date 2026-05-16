package com.hms.billing_service.dto.insurance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SubmitClaimRequestDTO(
    @NotNull UUID invoiceId,
    @NotNull UUID policyId,
    @NotNull @DecimalMin("0.01") BigDecimal claimedAmount) {}
