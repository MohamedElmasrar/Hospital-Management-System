package com.hms.billing_service.service;

import com.hms.billing_service.dto.report.RevenueReportResponseDTO;
import com.hms.billing_service.dto.report.UnpaidInvoiceResponseDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.model.Invoice;
import com.hms.billing_service.repository.InvoiceRepository;
import com.hms.billing_service.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingReportService {
  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;
  private final InvoiceService invoiceService;

  @Transactional(readOnly = true)
  public List<UnpaidInvoiceResponseDTO> unpaidInvoices() {
    return invoiceRepository.findAll().stream()
        .filter(invoice -> invoice.getStatus() != InvoiceStatus.VOID
            && invoiceService.calculate(invoice).balanceDue().signum() > 0)
        .map(this::toUnpaid)
        .toList();
  }

  @Transactional(readOnly = true)
  public RevenueReportResponseDTO revenue(
      LocalDate from, LocalDate to, UUID departmentId) {
    Date start = Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    var payments = paymentRepository.findAll().stream()
        .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
        .filter(payment -> !payment.getPaidAt().before(start) && payment.getPaidAt().before(end))
        .filter(payment -> departmentId == null || payment.getInvoice().getLineItems().stream()
            .anyMatch(item -> departmentId.equals(item.getBillableOrder().getDepartmentId())))
        .toList();
    BigDecimal total = payments.stream()
        .map(payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new RevenueReportResponseDTO(from, to, total, payments.size(), departmentId);
  }

  private UnpaidInvoiceResponseDTO toUnpaid(Invoice invoice) {
    var calculation = invoiceService.calculate(invoice);
    return new UnpaidInvoiceResponseDTO(
        invoice.getId(),
        invoice.getInvoiceNumber(),
        invoice.getPatientId(),
        calculation.totalAmount(),
        calculation.paidAmount(),
        calculation.balanceDue(),
        invoice.getDueDate());
  }
}
