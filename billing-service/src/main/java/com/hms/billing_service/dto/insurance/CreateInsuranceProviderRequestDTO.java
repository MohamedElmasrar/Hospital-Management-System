package com.hms.billing_service.dto.insurance;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateInsuranceProviderRequestDTO(
    @NotBlank @Length(max = 150) String name,
    @NotBlank @Length(max = 50) String code,
    @Email @Length(max = 100) String contactEmail,
    @Length(max = 50) String contactPhone) {}

