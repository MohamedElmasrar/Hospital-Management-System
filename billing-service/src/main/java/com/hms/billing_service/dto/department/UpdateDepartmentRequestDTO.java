package com.hms.billing_service.dto.department;

import com.hms.billing_service.enums.BillingSectionType;
import org.hibernate.validator.constraints.Length;

public record UpdateDepartmentRequestDTO(
    @Length(max = 100) String name, BillingSectionType section) {}
