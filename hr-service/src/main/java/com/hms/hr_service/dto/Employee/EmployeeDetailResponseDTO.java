package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EmployeeDetailResponseDTO(
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
    Date dateModified,
    List<ContactPointResponseDTO> contactPoints,
    PostalAddressDTO postalAddress,
    List<EmployeeAssignmentResponseDTO> assignments) {}
