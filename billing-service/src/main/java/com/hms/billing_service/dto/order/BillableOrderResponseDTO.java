package com.hms.billing_service.dto.order;

import com.hms.billing_service.dto.catalog.TariffVersionResponseDTO;
import com.hms.billing_service.enums.SourceOrderType;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BillableOrderResponseDTO(
    UUID id,
    UUID externalOrderId,
    SourceOrderType orderType,
    UUID patientId,
    UUID practitionerId,
    UUID serviceCatalogId,
    String serviceName,
    String serviceCode,
    TariffVersionResponseDTO tariffVersion,
    DiagnosticOrderDetailDTO diagnosticDetails,
    Date performedAt,
    Date dateCreated) {}
