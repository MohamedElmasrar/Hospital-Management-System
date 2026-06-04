package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.ContactType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record CreateContactPointRequestDTO(
    @NotNull ContactType contactType,
    @Length(max = 30) String telephone,
    @Length(max = 100) String email,
    boolean isPreferred) {}
