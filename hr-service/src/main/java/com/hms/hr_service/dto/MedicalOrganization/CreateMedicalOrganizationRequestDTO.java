package com.hms.hr_service.dto.MedicalOrganization;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateMedicalOrganizationRequestDTO(
    @NotNull @Length(max = 50) String name,
    @Length(max = 50) String legalName,
    @NotNull String identifier) {}
