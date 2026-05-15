package com.hms.billing_service.dto.insurance;

import com.hms.billing_service.enums.ClaimStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateClaimStatusRequestDTO(
    @NotNull ClaimStatus status, @DecimalMin("0.00") BigDecimal approvedAmount) {}
