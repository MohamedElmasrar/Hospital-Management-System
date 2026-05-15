package com.hms.billing_service.dto.department;

import com.hms.billing_service.enums.BillingSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateDepartmentRequestDTO(
    @NotBlank @Length(max = 100) String name, @NotNull BillingSectionType section) {}
