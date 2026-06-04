package com.hms.hr_service.dto.Department;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateDepartmentRequestDTO(
    @NotBlank @Length(max = 100) String name,
    @Length(max = 255) String description,
    UUID parentDepartmentId) {}
