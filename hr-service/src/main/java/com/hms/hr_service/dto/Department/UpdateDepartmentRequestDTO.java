package com.hms.hr_service.dto.Department;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record UpdateDepartmentRequestDTO(
    @NotBlank @Length(max = 100) String name,
    @Length(max = 255) String description,
    UUID parentDepartmentId) {}
