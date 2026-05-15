package com.hms.billing_service.dto.department;

import com.hms.billing_service.enums.BillingSectionType;
import java.util.Date;
import java.util.UUID;

public record DepartmentResponseDTO(
    UUID id, String name, BillingSectionType section, Date dateCreated, Date dateModified) {}
