package com.hms.billing_service.dto.cashdesk;

import com.hms.billing_service.enums.BillingSectionType;
import java.util.UUID;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateCashDeskRequestDTO(
    @Length(max = 30) String deskCode,
    UUID departmentId,
    BillingSectionType section,
    Boolean isActive) {}
