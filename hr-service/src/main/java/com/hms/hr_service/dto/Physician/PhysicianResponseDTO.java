package com.hms.hr_service.dto.Physician;

import java.util.List;
import java.util.UUID;

public record PhysicianResponseDTO(
    UUID id,
    UUID employeeId,
    String employeeIdentifier,
    String employeeName,
    String licenseNumber,
    List<PhysicianSpecialtyResponseDTO> specialties) {}
