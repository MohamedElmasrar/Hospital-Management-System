package com.hms.hr_service.dto.MedicalOrganization;

import lombok.Builder;

@Builder
public record CreateMedicalOrganizationResponseDTO(
    String id,
    String name,
    String legalName,
    String identifier,
    String createdAt,
    String updatedAt) {}
