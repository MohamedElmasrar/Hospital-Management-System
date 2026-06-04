package com.hms.billing_service.dto.invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InvoiceLineItemResponseDTO(
    UUID id,
    UUID billableOrderId,
    UUID serviceCatalogId,
    String serviceName,
    String serviceCode,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    Integer quantity,
    BigDecimal discountRate,
    BigDecimal lineSubtotal,
    BigDecimal lineDiscountAmount,
    BigDecimal lineTaxAmount,
    BigDecimal lineTotal,
    Date dateCreated) {}
