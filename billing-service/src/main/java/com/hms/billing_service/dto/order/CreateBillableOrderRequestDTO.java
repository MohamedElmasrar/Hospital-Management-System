package com.hms.billing_service.dto.order;

import com.hms.billing_service.enums.SourceOrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

public record CreateBillableOrderRequestDTO(
    @NotNull UUID externalOrderId,
    @NotNull SourceOrderType orderType,
    @NotNull UUID patientId,
    @NotNull UUID practitionerId,
    UUID tariffVersionId,
    UUID serviceCatalogId,
    Date performedAt,
    @Valid DiagnosticOrderDetailDTO diagnosticDetails) {}
