package com.hms.hr_service.dto.Employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record AssignEmployeeRequestDTO(
    @NotNull UUID departmentId,
    @NotBlank @Length(max = 100) String jobTitle,
    @NotNull Date startDate,
    Date endDate) {}
