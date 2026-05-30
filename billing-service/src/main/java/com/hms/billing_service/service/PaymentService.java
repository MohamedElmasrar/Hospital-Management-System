package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.dto.payment.ProcessPaymentRequestDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.exception.CashDeskNotFoundException;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.exception.InvoiceNotFoundException;
import com.hms.billing_service.exception.PaymentNotFoundException;
import com.hms.billing_service.mapper.PaymentMapper;
import com.hms.billing_service.messaging.AnalyticsEventPublisher;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.Invoice;
import com.hms.billing_service.model.Payment;
import com.hms.billing_service.repository.CashDeskRepository;
import com.hms.billing_service.repository.InvoiceRepository;
import com.hms.billing_service.repository.PaymentRepository;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final InvoiceRepository invoiceRepository;
  private final CashDeskRepository cashDeskRepository;
  private final InvoiceService invoiceService;
  private final PaymentMapper paymentMapper;
  private final com.hms.billing_service.grpc.HrGrpcClient hrGrpcClient;
  private final AnalyticsEventPublisher analyticsEventPublisher;

  @Transactional
  public PaymentResponseDTO processPayment(ProcessPaymentRequestDTO request, UUID cashierId) {
    if (cashierId != null && !hrGrpcClient.validateEmployee(cashierId)) {
      throw new InvalidOperationException(
          "Cashier does not exist or is inactive with ID: " + cashierId);
    }

    Invoice invoice =
        invoiceRepository
            .findById(request.invoiceId())
            .orElseThrow(
                () ->
                    new InvoiceNotFoundException(
                        "Invoice not found with ID: " + request.invoiceId()));

    if (invoice.getStatus() == InvoiceStatus.VOID) {
      throw new InvalidOperationException("Cannot accept payment for a void invoice");
    }

    if (invoice.getStatus() == InvoiceStatus.PAID) {
      throw new InvalidOperationException("Invoice has already been fully paid");
    }

    InvoiceService.InvoiceCalculations calc = invoiceService.calculate(invoice);
    if (request.amount().compareTo(calc.balanceDue()) > 0) {
      throw new InvalidOperationException(
          "Payment amount " + request.amount() + " exceeds balance due " + calc.balanceDue());
    }

    CashDesk cashDesk =
        cashDeskRepository
            .findById(request.cashDeskId())
            .orElseThrow(
                () ->
                    new CashDeskNotFoundException(
                        "Cash desk not found with ID: " + request.cashDeskId()));

    if (!Boolean.TRUE.equals(cashDesk.getIsActive())) {
      throw new InvalidOperationException("Cash desk is inactive: " + cashDesk.getDeskCode());
    }

    Payment payment = new Payment();
    payment.setInvoice(invoice);
    payment.setPaymentReference(generatePaymentReference());
    payment.setCashDesk(cashDesk);
    payment.setCashierId(cashierId);
    payment.setAmount(request.amount());
    payment.setMethod(request.method());
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setGatewayTransactionId(request.gatewayTransactionId());
    payment.setPaidAt(new Date());

    Payment saved = paymentRepository.save(payment);

    BigDecimal remainingBalance = calc.balanceDue().subtract(request.amount());
    if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
      invoice.setStatus(InvoiceStatus.PAID);
      invoiceRepository.save(invoice);
      analyticsEventPublisher.publish("INVOICE_PAID", "INVOICE", invoice.getId());
    } else if (invoice.getStatus() == InvoiceStatus.DRAFT) {
      invoice.setStatus(InvoiceStatus.ISSUED);
      invoiceRepository.save(invoice);
      analyticsEventPublisher.publish("INVOICE_ISSUED", "INVOICE", invoice.getId());
    }

    analyticsEventPublisher.publish(
        "PAYMENT_COMPLETED",
        "PAYMENT",
        saved.getId(),
        saved.getAmount(),
        "method",
        saved.getMethod().name());

    return paymentMapper.paymentToPaymentResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public PaymentResponseDTO getById(UUID id) {
    Payment payment =
        paymentRepository
            .findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id));
    return paymentMapper.paymentToPaymentResponseDTO(payment);
  }

  @Transactional(readOnly = true)
  public List<PaymentResponseDTO> getByInvoice(UUID invoiceId) {
    if (!invoiceRepository.existsById(invoiceId)) {
      throw new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId);
    }
    return paymentMapper.toPaymentResponseDTOList(paymentRepository.findByInvoiceId(invoiceId));
  }

  @Transactional(readOnly = true)
  public PageResponse<PaymentResponseDTO> getByCashDesk(UUID cashDeskId, Pageable pageable) {
    return PageResponse.from(
        paymentRepository
            .findByCashDeskId(cashDeskId, pageable)
            .map(paymentMapper::paymentToPaymentResponseDTO));
  }

  @Transactional
  public PaymentResponseDTO refundPayment(UUID paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

    if (payment.getStatus() == PaymentStatus.REFUNDED) {
      throw new InvalidOperationException("Payment is already refunded");
    }

    payment.setStatus(PaymentStatus.REFUNDED);
    Payment updated = paymentRepository.save(payment);

    Invoice invoice = payment.getInvoice();
    InvoiceService.InvoiceCalculations calc = invoiceService.calculate(invoice);
    if (calc.balanceDue().compareTo(BigDecimal.ZERO) > 0
        && invoice.getStatus() == InvoiceStatus.PAID) {
      invoice.setStatus(InvoiceStatus.ISSUED);
      invoiceRepository.save(invoice);
      analyticsEventPublisher.publish("INVOICE_ISSUED", "INVOICE", invoice.getId());
    }

    analyticsEventPublisher.publish(
        "PAYMENT_REFUNDED",
        "PAYMENT",
        updated.getId(),
        updated.getAmount(),
        "method",
        updated.getMethod().name());

    return paymentMapper.paymentToPaymentResponseDTO(updated);
  }

  private String generatePaymentReference() {
    String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    return "PAY-" + datePart + "-" + randomPart;
  }
}
