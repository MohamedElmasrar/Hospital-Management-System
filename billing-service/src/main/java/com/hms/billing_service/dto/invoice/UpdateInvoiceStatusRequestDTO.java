package com.hms.billing_service.dto.invoice;

import com.hms.billing_service.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceStatusRequestDTO(@NotNull InvoiceStatus status) {}
