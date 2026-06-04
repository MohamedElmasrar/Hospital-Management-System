package com.hms.hr_service.dto.Employee;

import java.util.Date;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EmployeeAssignmentResponseDTO(
    UUID id,
    UUID employeeId,
    String employeeIdentifier,
    String employeeName,
    UUID departmentId,
    String departmentName,
    String jobTitle,
    Date startDate,
    Date endDate) {}
