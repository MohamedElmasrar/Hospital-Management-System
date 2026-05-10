package com.hms.hr_service.dto.MedicalOrganization;

import org.hibernate.validator.constraints.Length;

public record UpdateMedicalOrganizationRequestDTO(
    @Length(max = 50) String name,
    @Length(max = 50) String legalName,
    @Length(max = 50) String identifier) {}
