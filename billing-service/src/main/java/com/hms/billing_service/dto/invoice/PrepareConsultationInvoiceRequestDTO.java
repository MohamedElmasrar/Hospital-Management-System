package com.hms.billing_service.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(name = "PrepareConsultationInvoiceRequest")
public record PrepareConsultationInvoiceRequestDTO(
    @NotNull UUID serviceCatalogId, @NotNull UUID cashDeskId) {}
