package com.hms.hr_service.dto.Employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateAssignedEmployeeRequestDTO(
    @NotNull @Valid CreateEmployeeRequestDTO employee,
    @NotNull @Valid AssignEmployeeRequestDTO assignment) {}
