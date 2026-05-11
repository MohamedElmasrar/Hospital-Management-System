package com.hms.hr_service.dto.Physician;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdatePhysicianRequestDTO(@NotBlank @Length(max = 50) String licenseNumber) {}
