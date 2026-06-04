package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.AddressType;
import com.hms.hr_service.enums.ContactType;
import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record CreateEmployeeRequestDTO(
    @NotBlank @Length(max = 50) String identifier,
    @NotBlank @Length(max = 100) String givenName,
    @Length(max = 100) String additionalName,
    @NotBlank @Length(max = 100) String familyName,
    @Length(max = 20) String honorificPrefix,
    Date birthDate,
    GenderType gender,
    @NotNull EmploymentStatus employmentStatus,
    // Optional initial contact info
    String email,
    String telephone,
    ContactType contactType,
    // Optional initial address info
    String streetAddress,
    String addressLocality,
    String addressRegion,
    String postalCode,
    String addressCountry,
    AddressType addressType) {}
