package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import java.util.Date;
import java.util.UUID;

public record EmployeeResponseDTO(
    UUID id,
    String identifier,
    String givenName,
    String additionalName,
    String familyName,
    String honorificPrefix,
    Date birthDate,
    GenderType gender,
    EmploymentStatus employmentStatus,
    Date dateCreated,
    Date dateModified) {}
