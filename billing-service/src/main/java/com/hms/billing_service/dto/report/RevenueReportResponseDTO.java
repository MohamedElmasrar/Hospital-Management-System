package com.hms.billing_service.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RevenueReportResponseDTO(
    LocalDate from, LocalDate to, BigDecimal totalRevenue, long paymentCount, UUID departmentId) {}
