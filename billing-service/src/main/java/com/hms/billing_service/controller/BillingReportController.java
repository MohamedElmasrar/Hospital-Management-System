package com.hms.billing_service.controller;

import com.hms.billing_service.dto.report.RevenueReportResponseDTO;
import com.hms.billing_service.dto.report.UnpaidInvoiceResponseDTO;
import com.hms.billing_service.service.BillingReportService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class BillingReportController {
  private final BillingReportService billingReportService;

  @GetMapping("/unpaid-invoices")
  public ResponseEntity<List<UnpaidInvoiceResponseDTO>> unpaidInvoices() {
    return ResponseEntity.ok(billingReportService.unpaidInvoices());
  }

  @GetMapping("/revenue")
  public ResponseEntity<RevenueReportResponseDTO> revenue(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) UUID departmentId) {
    return ResponseEntity.ok(billingReportService.revenue(from, to, departmentId));
  }
}
