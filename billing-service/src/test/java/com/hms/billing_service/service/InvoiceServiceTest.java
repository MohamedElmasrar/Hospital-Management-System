package com.hms.billing_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.hms.billing_service.dto.invoice.CreateInvoiceLineItemRequestDTO;
import com.hms.billing_service.dto.invoice.CreateInvoiceRequestDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.enums.SourceOrderType;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.grpc.HrGrpcClient;
import com.hms.billing_service.mapper.InsuranceMapper;
import com.hms.billing_service.mapper.InvoiceMapper;
import com.hms.billing_service.mapper.PaymentMapper;
import com.hms.billing_service.messaging.AnalyticsEventPublisher;
import com.hms.billing_service.model.*;
import com.hms.billing_service.repository.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceLineItemRepository lineItemRepository;
  @Mock private CashDeskRepository cashDeskRepository;
  @Mock private BillableOrderRepository billableOrderRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private InsuranceClaimRepository insuranceClaimRepository;
  @Mock private HrGrpcClient hrGrpcClient;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;

  private InvoiceMapper invoiceMapper;
  private PaymentMapper paymentMapper;
  private InsuranceMapper insuranceMapper;
  private InvoiceService invoiceService;

  @BeforeEach
  void setUp() {
    invoiceMapper = Mappers.getMapper(InvoiceMapper.class);
    paymentMapper = Mappers.getMapper(PaymentMapper.class);
    insuranceMapper = Mappers.getMapper(InsuranceMapper.class);
    invoiceService =
        new InvoiceService(
            invoiceRepository,
            lineItemRepository,
            cashDeskRepository,
            billableOrderRepository,
            paymentRepository,
            insuranceClaimRepository,
            invoiceMapper,
            paymentMapper,
            insuranceMapper,
            analyticsEventPublisher);
  }

  @Test
  void calculate_accurateFinancialBreakdown() {
    Invoice invoice = new Invoice();
    invoice.setId(UUID.randomUUID());

    ServiceCatalog catalog = new ServiceCatalog();
    catalog.setName("MRI Brain Scan");
    catalog.setServiceCode("IMG-MRI-BRAIN");

    ServiceTariffVersion tariff = new ServiceTariffVersion();
    tariff.setCatalogItem(catalog);
    tariff.setUnitPrice(new BigDecimal("500.00"));
    tariff.setTaxRate(new BigDecimal("0.1000")); // 10% tax

    BillableOrder order = new BillableOrder();
    order.setId(UUID.randomUUID());
    order.setOrderType(SourceOrderType.DIAGNOSTIC_ORDER);
    order.setTariffVersion(tariff);

    InvoiceLineItem item = new InvoiceLineItem();
    item.setId(UUID.randomUUID());
    item.setBillableOrder(order);
    item.setQuantity(2); // 2 * 500 = 1000.00
    item.setDiscountRate(
        new BigDecimal(
            "0.1000")); // 10% discount = 100.00 discount -> 900 taxable -> 90 tax -> 990 total
    invoice.addLineItem(item);

    when(paymentRepository.findByInvoiceIdAndStatus(invoice.getId(), PaymentStatus.COMPLETED))
        .thenReturn(Collections.emptyList());
    when(insuranceClaimRepository.findByInvoiceId(invoice.getId())).thenReturn(Optional.empty());

    InvoiceService.InvoiceCalculations calc = invoiceService.calculate(invoice);

    assertThat(calc.subtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
    assertThat(calc.totalDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(calc.totalTax()).isEqualByComparingTo(new BigDecimal("90.00"));
    assertThat(calc.totalAmount()).isEqualByComparingTo(new BigDecimal("990.00"));
    assertThat(calc.paidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(calc.balanceDue()).isEqualByComparingTo(new BigDecimal("990.00"));
  }

  @Test
  void voidInvoice_withCompletedPayments_throwsException() {
    UUID invoiceId = UUID.randomUUID();
    Invoice invoice = new Invoice();
    invoice.setId(invoiceId);

    Payment payment = new Payment();
    payment.setAmount(new BigDecimal("50.00"));

    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(paymentRepository.findByInvoiceIdAndStatus(invoiceId, PaymentStatus.COMPLETED))
        .thenReturn(List.of(payment));

    assertThatThrownBy(() -> invoiceService.voidInvoice(invoiceId))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("Cannot void invoice that has completed payments");
  }

  @Test
  void create_orderPatientMismatch_throwsException() {
    UUID patientA = UUID.randomUUID();
    UUID patientB = UUID.randomUUID();
    UUID deskId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    CashDesk desk = new CashDesk();
    desk.setId(deskId);
    desk.setIsActive(true);

    BillableOrder order = new BillableOrder();
    order.setId(orderId);
    order.setPatientId(patientB);

    when(cashDeskRepository.findById(deskId)).thenReturn(Optional.of(desk));
    when(lineItemRepository.existsByBillableOrderId(orderId)).thenReturn(false);
    when(billableOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

    CreateInvoiceRequestDTO request =
        CreateInvoiceRequestDTO.builder()
            .patientId(patientA)
            .cashDeskId(deskId)
            .lineItems(
                List.of(
                    CreateInvoiceLineItemRequestDTO.builder()
                        .billableOrderId(orderId)
                        .quantity(1)
                        .discountRate(BigDecimal.ZERO)
                        .build()))
            .build();

    assertThatThrownBy(() -> invoiceService.create(request, UUID.randomUUID()))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("does not belong to patient");
  }

  @Test
  void voidInvoice_alreadyVoid_doesNotCountAgain() {
    UUID invoiceId = UUID.randomUUID();
    Invoice invoice = new Invoice();
    invoice.setId(invoiceId);
    invoice.setStatus(InvoiceStatus.VOID);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

    invoiceService.voidInvoice(invoiceId);

    verifyNoInteractions(analyticsEventPublisher);
  }
}
