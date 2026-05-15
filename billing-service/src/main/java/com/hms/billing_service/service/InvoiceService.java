package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.insurance.InsuranceClaimResponseDTO;
import com.hms.billing_service.dto.invoice.*;
import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.enums.ClaimStatus;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.exception.*;
import com.hms.billing_service.mapper.InsuranceMapper;
import com.hms.billing_service.mapper.InvoiceMapper;
import com.hms.billing_service.mapper.PaymentMapper;
import com.hms.billing_service.model.*;
import com.hms.billing_service.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceLineItemRepository lineItemRepository;
  private final CashDeskRepository cashDeskRepository;
  private final BillableOrderRepository billableOrderRepository;
  private final PaymentRepository paymentRepository;
  private final InsuranceClaimRepository insuranceClaimRepository;
  private final InvoiceMapper invoiceMapper;
  private final PaymentMapper paymentMapper;
  private final InsuranceMapper insuranceMapper;

  @Transactional
  public InvoiceDetailResponseDTO create(CreateInvoiceRequestDTO request, UUID staffId) {
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

    Invoice invoice = new Invoice();
    invoice.setInvoiceNumber(generateInvoiceNumber());
    invoice.setPatientId(request.patientId());
    invoice.setCashDesk(cashDesk);
    invoice.setIssuedByStaffId(staffId);
    invoice.setStatus(InvoiceStatus.DRAFT);
    invoice.setIssuedAt(new Date());
    invoice.setDueDate(request.dueDate());

    for (CreateInvoiceLineItemRequestDTO itemDTO : request.lineItems()) {
      if (lineItemRepository.existsByBillableOrderId(itemDTO.billableOrderId())) {
        throw new ResourceAlreadyExistsException(
            "Billable order has already been billed: " + itemDTO.billableOrderId());
      }
      BillableOrder order =
          billableOrderRepository
              .findById(itemDTO.billableOrderId())
              .orElseThrow(
                  () ->
                      new BillableOrderNotFoundException(
                          "Billable order not found: " + itemDTO.billableOrderId()));

      if (!order.getPatientId().equals(request.patientId())) {
        throw new InvalidOperationException(
            "Billable order "
                + order.getId()
                + " does not belong to patient "
                + request.patientId());
      }

      InvoiceLineItem lineItem = new InvoiceLineItem();
      lineItem.setBillableOrder(order);
      lineItem.setQuantity(itemDTO.quantity() != null ? itemDTO.quantity() : 1);
      lineItem.setDiscountRate(
          itemDTO.discountRate() != null ? itemDTO.discountRate() : BigDecimal.ZERO);
      invoice.addLineItem(lineItem);
    }

    Invoice saved = invoiceRepository.save(invoice);
    return getDetailDTO(saved);
  }

  @Transactional
  public InvoiceDetailResponseDTO addLineItem(UUID invoiceId, AddLineItemRequestDTO request) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(
                () -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

    if (invoice.getStatus() != InvoiceStatus.DRAFT) {
      throw new InvalidOperationException(
          "Cannot add line items to an invoice with status " + invoice.getStatus());
    }

    if (lineItemRepository.existsByBillableOrderId(request.billableOrderId())) {
      throw new ResourceAlreadyExistsException(
          "Billable order has already been billed: " + request.billableOrderId());
    }

    BillableOrder order =
        billableOrderRepository
            .findById(request.billableOrderId())
            .orElseThrow(
                () ->
                    new BillableOrderNotFoundException(
                        "Billable order not found: " + request.billableOrderId()));

    if (!order.getPatientId().equals(invoice.getPatientId())) {
      throw new InvalidOperationException(
          "Billable order does not belong to invoice patient: " + invoice.getPatientId());
    }

    InvoiceLineItem lineItem = new InvoiceLineItem();
    lineItem.setBillableOrder(order);
    lineItem.setQuantity(request.quantity());
    lineItem.setDiscountRate(
        request.discountRate() != null ? request.discountRate() : BigDecimal.ZERO);
    invoice.addLineItem(lineItem);

    Invoice saved = invoiceRepository.save(invoice);
    return getDetailDTO(saved);
  }

  @Transactional
  public InvoiceDetailResponseDTO removeLineItem(UUID invoiceId, UUID lineItemId) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(
                () -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

    if (invoice.getStatus() != InvoiceStatus.DRAFT) {
      throw new InvalidOperationException(
          "Cannot remove line items from an invoice with status " + invoice.getStatus());
    }

    InvoiceLineItem itemToRemove =
        invoice.getLineItems().stream()
            .filter(item -> item.getId().equals(lineItemId))
            .findFirst()
            .orElseThrow(
                () -> new InvalidOperationException("Line item not found on this invoice"));

    invoice.removeLineItem(itemToRemove);
    Invoice saved = invoiceRepository.save(invoice);
    return getDetailDTO(saved);
  }

  @Transactional
  public InvoiceDetailResponseDTO updateStatus(UUID invoiceId, InvoiceStatus newStatus) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(
                () -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

    if (invoice.getStatus() == InvoiceStatus.VOID && newStatus != InvoiceStatus.VOID) {
      throw new InvalidOperationException("Cannot change status of a voided invoice");
    }

    if (invoice.getStatus() == InvoiceStatus.PAID && newStatus == InvoiceStatus.DRAFT) {
      throw new InvalidOperationException("Cannot revert a paid invoice to draft");
    }

    invoice.setStatus(newStatus);
    Invoice saved = invoiceRepository.save(invoice);
    return getDetailDTO(saved);
  }

  @Transactional(readOnly = true)
  public InvoiceDetailResponseDTO getById(UUID id) {
    Invoice invoice =
        invoiceRepository
            .findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));
    return getDetailDTO(invoice);
  }

  @Transactional(readOnly = true)
  public InvoiceDetailResponseDTO getByInvoiceNumber(String invoiceNumber) {
    Invoice invoice =
        invoiceRepository
            .findByInvoiceNumber(invoiceNumber)
            .orElseThrow(
                () ->
                    new InvoiceNotFoundException(
                        "Invoice not found with invoiceNumber: " + invoiceNumber));
    return getDetailDTO(invoice);
  }

  @Transactional(readOnly = true)
  public PageResponse<InvoiceSummaryResponseDTO> search(
      UUID patientId, InvoiceStatus status, Pageable pageable) {
    if (patientId != null && status != null) {
      return PageResponse.from(
          invoiceRepository
              .findByPatientIdAndStatus(patientId, status, pageable)
              .map(this::getSummaryDTO));
    } else if (patientId != null) {
      return PageResponse.from(
          invoiceRepository.findByPatientId(patientId, pageable).map(this::getSummaryDTO));
    } else if (status != null) {
      return PageResponse.from(
          invoiceRepository.findByStatus(status, pageable).map(this::getSummaryDTO));
    } else {
      return PageResponse.from(invoiceRepository.findAll(pageable).map(this::getSummaryDTO));
    }
  }

  @Transactional
  public void voidInvoice(UUID id) {
    Invoice invoice =
        invoiceRepository
            .findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + id));

    List<Payment> completedPayments =
        paymentRepository.findByInvoiceIdAndStatus(id, PaymentStatus.COMPLETED);
    if (!completedPayments.isEmpty()) {
      throw new InvalidOperationException(
          "Cannot void invoice that has completed payments. Refund payments first.");
    }

    invoice.setStatus(InvoiceStatus.VOID);
    invoiceRepository.save(invoice);
  }

  // --- Calculation helpers ---

  public InvoiceSummaryResponseDTO getSummaryDTO(Invoice invoice) {
    InvoiceCalculations calc = calculate(invoice);
    return invoiceMapper.toSummaryResponseDTO(
        invoice,
        calc.subtotal,
        calc.totalDiscount,
        calc.totalTax,
        calc.totalAmount,
        calc.paidAmount,
        calc.balanceDue);
  }

  public InvoiceDetailResponseDTO getDetailDTO(Invoice invoice) {
    InvoiceCalculations calc = calculate(invoice);

    List<InvoiceLineItemResponseDTO> lineItemDTOs =
        calc.lineCalculations.stream()
            .map(
                lc ->
                    invoiceMapper.toLineItemResponseDTO(
                        lc.item, lc.subtotal, lc.discount, lc.tax, lc.total))
            .toList();

    List<PaymentResponseDTO> paymentDTOs =
        paymentMapper.toPaymentResponseDTOList(paymentRepository.findByInvoiceId(invoice.getId()));

    InsuranceClaimResponseDTO claimDTO =
        insuranceClaimRepository
            .findByInvoiceId(invoice.getId())
            .map(insuranceMapper::claimToResponseDTO)
            .orElse(null);

    return invoiceMapper.toDetailResponseDTO(
        invoice,
        calc.subtotal,
        calc.totalDiscount,
        calc.totalTax,
        calc.totalAmount,
        calc.paidAmount,
        calc.balanceDue,
        lineItemDTOs,
        paymentDTOs,
        claimDTO);
  }

  public InvoiceCalculations calculate(Invoice invoice) {
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    List<LineCalculation> lineCalculations = new ArrayList<>();

    for (InvoiceLineItem item : invoice.getLineItems()) {
      BigDecimal unitPrice = item.getBillableOrder().getTariffVersion().getUnitPrice();
      BigDecimal taxRate = item.getBillableOrder().getTariffVersion().getTaxRate();
      BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
      BigDecimal discountRate = item.getDiscountRate();

      BigDecimal lineSubtotal = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
      BigDecimal lineDiscount =
          lineSubtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
      BigDecimal taxable = lineSubtotal.subtract(lineDiscount);
      BigDecimal lineTax = taxable.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
      BigDecimal lineTotal = taxable.add(lineTax).setScale(2, RoundingMode.HALF_UP);

      subtotal = subtotal.add(lineSubtotal);
      totalDiscount = totalDiscount.add(lineDiscount);
      totalTax = totalTax.add(lineTax);
      totalAmount = totalAmount.add(lineTotal);

      lineCalculations.add(
          new LineCalculation(item, lineSubtotal, lineDiscount, lineTax, lineTotal));
    }

    BigDecimal paidAmount =
        paymentRepository
            .findByInvoiceIdAndStatus(invoice.getId(), PaymentStatus.COMPLETED)
            .stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal insuranceApproved =
        insuranceClaimRepository
            .findByInvoiceId(invoice.getId())
            .filter(
                c -> c.getStatus() == ClaimStatus.APPROVED || c.getStatus() == ClaimStatus.SETTLED)
            .map(c -> c.getApprovedAmount() != null ? c.getApprovedAmount() : BigDecimal.ZERO)
            .orElse(BigDecimal.ZERO);

    BigDecimal balanceDue =
        totalAmount
            .subtract(paidAmount)
            .subtract(insuranceApproved)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

    return new InvoiceCalculations(
        subtotal, totalDiscount, totalTax, totalAmount, paidAmount, balanceDue, lineCalculations);
  }

  private String generateInvoiceNumber() {
    String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
    return "INV-" + datePart + "-" + randomPart;
  }

  public record LineCalculation(
      InvoiceLineItem item,
      BigDecimal subtotal,
      BigDecimal discount,
      BigDecimal tax,
      BigDecimal total) {}

  public record InvoiceCalculations(
      BigDecimal subtotal,
      BigDecimal totalDiscount,
      BigDecimal totalTax,
      BigDecimal totalAmount,
      BigDecimal paidAmount,
      BigDecimal balanceDue,
      List<LineCalculation> lineCalculations) {}
}
