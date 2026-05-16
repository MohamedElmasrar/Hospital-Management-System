package com.hms.billing_service.dto.cashdesk;

import com.hms.billing_service.enums.BillingSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.hibernate.validator.constraints.Length;

public record CreateCashDeskRequestDTO(
    @NotBlank @Length(max = 30) String deskCode,
    @NotNull UUID departmentId,
    @NotNull BillingSectionType section) {}
