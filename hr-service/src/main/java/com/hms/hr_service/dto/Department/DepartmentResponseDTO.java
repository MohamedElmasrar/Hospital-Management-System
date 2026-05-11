package com.hms.hr_service.dto.Department;

import java.util.Date;
import java.util.UUID;

public record DepartmentResponseDTO(
    UUID id,
    UUID medicalOrganizationId,
    String medicalOrganizationName,
    UUID parentDepartmentId,
    String parentDepartmentName,
    String name,
    String description,
    Date dateCreated) {}
