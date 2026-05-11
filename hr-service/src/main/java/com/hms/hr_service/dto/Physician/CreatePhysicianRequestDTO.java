package com.hms.hr_service.dto.Physician;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record CreatePhysicianRequestDTO(
    @NotNull UUID employeeId,
    @NotBlank @Length(max = 50) String licenseNumber,
    List<UUID> specialtyIds,
    UUID primarySpecialtyId) {}
