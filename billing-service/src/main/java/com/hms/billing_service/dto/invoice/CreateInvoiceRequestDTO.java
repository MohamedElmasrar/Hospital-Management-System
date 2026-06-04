package com.hms.billing_service.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateInvoiceRequestDTO(
    @NotNull UUID patientId,
    @NotNull UUID cashDeskId,
    Date dueDate,
    @NotEmpty List<@Valid CreateInvoiceLineItemRequestDTO> lineItems) {}
