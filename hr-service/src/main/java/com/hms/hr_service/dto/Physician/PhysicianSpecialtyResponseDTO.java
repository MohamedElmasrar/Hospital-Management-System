package com.hms.hr_service.dto.Physician;

import java.util.UUID;

public record PhysicianSpecialtyResponseDTO(
    UUID specialtyId, String specialtyName, String description, boolean isPrimary) {}
