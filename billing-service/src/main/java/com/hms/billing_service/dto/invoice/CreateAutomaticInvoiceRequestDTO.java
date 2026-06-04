package com.hms.billing_service.dto.invoice;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateAutomaticInvoiceRequestDTO(
    @NotNull UUID patientId, @NotNull UUID cashDeskId, Date dueDate) {}
