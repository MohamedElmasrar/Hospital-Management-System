package com.hms.hr_service.dto.MedicalOrganization;

import jakarta.validation.constraints.NotNull;

public record MedicalOrganizationResponseDTO(
    @NotNull String id,
    @NotNull String name,
    String legalName,
    String identifier,
    String createdAt,
    String updatedAt) {}
