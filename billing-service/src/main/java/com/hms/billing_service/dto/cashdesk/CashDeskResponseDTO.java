package com.hms.billing_service.dto.cashdesk;

import java.util.Date;
import java.util.UUID;

public record CashDeskResponseDTO(
    UUID id,
    String deskCode,
    UUID departmentId,
    String departmentName,
    Boolean isActive,
    Date dateCreated,
    Date dateModified) {}

