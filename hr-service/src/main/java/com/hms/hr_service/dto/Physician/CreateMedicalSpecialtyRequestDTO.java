package com.hms.hr_service.dto.Physician;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateMedicalSpecialtyRequestDTO(
    @NotBlank @Length(max = 100) String name, @Length(max = 255) String description) {}
