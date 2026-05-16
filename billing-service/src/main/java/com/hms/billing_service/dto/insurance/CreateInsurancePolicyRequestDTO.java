package com.hms.billing_service.dto.insurance;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record CreateInsurancePolicyRequestDTO(
    @NotBlank @Length(max = 100) String policyNumber,
    @NotNull UUID insuranceProviderId,
    @NotNull UUID patientId,
    @NotNull @DecimalMin("0.0000") @DecimalMax("1.0000") BigDecimal coverageRate) {}
