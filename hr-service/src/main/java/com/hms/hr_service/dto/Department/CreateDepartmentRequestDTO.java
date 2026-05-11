package com.hms.hr_service.dto.Department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record CreateDepartmentRequestDTO(
    @NotNull UUID medicalOrganizationId,
    UUID parentDepartmentId,
    @NotBlank @Length(max = 100) String name,
    @Length(max = 255) String description) {}
