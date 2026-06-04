package com.hms.billing_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.dto.payment.ProcessPaymentRequestDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.PaymentMethod;
import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.grpc.HrGrpcClient;
import com.hms.billing_service.mapper.PaymentMapper;
import com.hms.billing_service.messaging.AnalyticsEventPublisher;
import com.hms.billing_service.messaging.NotificationEventPublisher;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.Invoice;
import com.hms.billing_service.model.Payment;
import com.hms.billing_service.repository.CashDeskRepository;
import com.hms.billing_service.repository.InvoiceRepository;
import com.hms.billing_service.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private CashDeskRepository cashDeskRepository;
  @Mock private InvoiceService invoiceService;
  @Mock private HrGrpcClient hrGrpcClient;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;

  @Mock private NotificationEventPublisher notificationEventPublisher;
  private PaymentMapper paymentMapper;
  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    paymentMapper = Mappers.getMapper(PaymentMapper.class);
    paymentService =
        new PaymentService(
            paymentRepository,
            invoiceRepository,
            cashDeskRepository,
            invoiceService,
            paymentMapper,
            analyticsEventPublisher,
            notificationEventPublisher);
  }

  @Test
  void processPayment_exceedsBalanceDue_throwsException() {
    UUID invoiceId = UUID.randomUUID();
    UUID cashDeskId = UUID.randomUUID();

    Invoice invoice = new Invoice();
    invoice.setId(invoiceId);
    invoice.setStatus(InvoiceStatus.ISSUED);

    when(invoiceRepository.findOneById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceService.calculate(invoice))
        .thenReturn(
            new InvoiceService.InvoiceCalculations(
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("50.00"), // balance due is only 50
                Collections.emptyList()));

    ProcessPaymentRequestDTO request =
        new ProcessPaymentRequestDTO(
            invoiceId, cashDeskId, new BigDecimal("60.00"), PaymentMethod.CASH, null);

    assertThatThrownBy(() -> paymentService.processPayment(request, UUID.randomUUID()))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("exceeds balance due");
  }

  @Test
  void processPayment_exactBalance_marksInvoicePaid() {
    UUID invoiceId = UUID.randomUUID();
    UUID cashDeskId = UUID.randomUUID();

    Invoice invoice = new Invoice();
    invoice.setId(invoiceId);
    invoice.setStatus(InvoiceStatus.ISSUED);
    invoice.setInvoiceNumber("INV-20261015-ABCDE");

    CashDesk desk = new CashDesk();
    desk.setId(cashDeskId);
    desk.setDeskCode("DESK-01");
    desk.setIsActive(true);

    when(invoiceRepository.findOneById(invoiceId)).thenReturn(Optional.of(invoice));
    when(invoiceService.calculate(invoice))
        .thenReturn(
            new InvoiceService.InvoiceCalculations(
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                Collections.emptyList()));
    when(cashDeskRepository.findById(cashDeskId)).thenReturn(Optional.of(desk));

    Payment savedPayment = new Payment();
    savedPayment.setId(UUID.randomUUID());
    savedPayment.setInvoice(invoice);
    savedPayment.setCashDesk(desk);
    savedPayment.setAmount(new BigDecimal("100.00"));
    savedPayment.setMethod(PaymentMethod.CARD);
    savedPayment.setStatus(PaymentStatus.COMPLETED);
    savedPayment.setPaymentReference("PAY-20261015-XYZ");
    savedPayment.setPaidAt(new Date());

    when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

    ProcessPaymentRequestDTO request =
        new ProcessPaymentRequestDTO(
            invoiceId, cashDeskId, new BigDecimal("100.00"), PaymentMethod.CARD, "txn-12345");

    PaymentResponseDTO response = paymentService.processPayment(request, UUID.randomUUID());

    assertThat(response).isNotNull();
    assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    verify(invoiceRepository).save(invoice);
    verify(analyticsEventPublisher).publish("INVOICE_PAID", "INVOICE", invoiceId);
    verify(analyticsEventPublisher)
        .publish(
            "PAYMENT_COMPLETED",
            "PAYMENT",
            savedPayment.getId(),
            new BigDecimal("100.00"),
            "method",
            "CARD");
  }
}
