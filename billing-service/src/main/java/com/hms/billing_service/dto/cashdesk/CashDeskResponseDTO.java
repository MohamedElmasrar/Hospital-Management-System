package com.hms.billing_service.dto.cashdesk;

import com.hms.billing_service.enums.BillingSectionType;
import java.util.Date;
import java.util.UUID;

public record CashDeskResponseDTO(
    UUID id,
    String deskCode,
    UUID departmentId,
    String departmentName,
    BillingSectionType section,
    Boolean isActive,
    Date dateCreated,
    Date dateModified) {}
