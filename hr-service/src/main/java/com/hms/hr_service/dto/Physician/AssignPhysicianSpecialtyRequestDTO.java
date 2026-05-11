package com.hms.hr_service.dto.Physician;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignPhysicianSpecialtyRequestDTO(
    @NotNull UUID medicalSpecialtyId, boolean isPrimary) {}
