package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import org.hibernate.validator.constraints.Length;

public record UpdateEmployeeRequestDTO(
    @NotBlank @Length(max = 100) String givenName,
    @Length(max = 100) String additionalName,
    @NotBlank @Length(max = 100) String familyName,
    @Length(max = 20) String honorificPrefix,
    Date birthDate,
    GenderType gender,
    EmploymentStatus employmentStatus) {}
