package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.insurance.InsuranceClaimResponseDTO;
import com.hms.billing_service.dto.invoice.InvoiceDetailResponseDTO;
import com.hms.billing_service.dto.invoice.InvoiceLineItemResponseDTO;
import com.hms.billing_service.dto.invoice.InvoiceSummaryResponseDTO;
import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.model.Invoice;
import com.hms.billing_service.model.InvoiceLineItem;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceMapper {

  @Mapping(source = "lineItem.id", target = "id")
  @Mapping(source = "lineItem.billableOrder.id", target = "billableOrderId")
  @Mapping(
      source = "lineItem.billableOrder.tariffVersion.catalogItem.id",
      target = "serviceCatalogId")
  @Mapping(source = "lineItem.billableOrder.tariffVersion.catalogItem.name", target = "serviceName")
  @Mapping(
      source = "lineItem.billableOrder.tariffVersion.catalogItem.serviceCode",
      target = "serviceCode")
  @Mapping(source = "lineItem.billableOrder.tariffVersion.unitPrice", target = "unitPrice")
  @Mapping(source = "lineItem.billableOrder.tariffVersion.taxRate", target = "taxRate")
  @Mapping(source = "lineItem.quantity", target = "quantity")
  @Mapping(source = "lineItem.discountRate", target = "discountRate")
  @Mapping(source = "subtotal", target = "lineSubtotal")
  @Mapping(source = "discountAmount", target = "lineDiscountAmount")
  @Mapping(source = "taxAmount", target = "lineTaxAmount")
  @Mapping(source = "total", target = "lineTotal")
  @Mapping(source = "lineItem.dateCreated", target = "dateCreated")
  InvoiceLineItemResponseDTO toLineItemResponseDTO(
      InvoiceLineItem lineItem,
      BigDecimal subtotal,
      BigDecimal discountAmount,
      BigDecimal taxAmount,
      BigDecimal total);

  @Mapping(source = "invoice.id", target = "id")
  @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "invoice.patientId", target = "patientId")
  @Mapping(source = "invoice.cashDesk.id", target = "cashDeskId")
  @Mapping(source = "invoice.cashDesk.deskCode", target = "cashDeskCode")
  @Mapping(source = "invoice.issuedByStaffId", target = "issuedByStaffId")
  @Mapping(source = "invoice.status", target = "status")
  @Mapping(source = "invoice.issuedAt", target = "issuedAt")
  @Mapping(source = "invoice.dueDate", target = "dueDate")
  @Mapping(source = "invoice.dateCreated", target = "dateCreated")
  @Mapping(source = "invoice.dateModified", target = "dateModified")
  @Mapping(source = "subtotal", target = "subtotal")
  @Mapping(source = "totalDiscount", target = "totalDiscount")
  @Mapping(source = "totalTax", target = "totalTax")
  @Mapping(source = "totalAmount", target = "totalAmount")
  @Mapping(source = "paidAmount", target = "paidAmount")
  @Mapping(source = "balanceDue", target = "balanceDue")
  InvoiceSummaryResponseDTO toSummaryResponseDTO(
      Invoice invoice,
      BigDecimal subtotal,
      BigDecimal totalDiscount,
      BigDecimal totalTax,
      BigDecimal totalAmount,
      BigDecimal paidAmount,
      BigDecimal balanceDue);

  @Mapping(source = "invoice.id", target = "id")
  @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "invoice.patientId", target = "patientId")
  @Mapping(source = "invoice.cashDesk.id", target = "cashDeskId")
  @Mapping(source = "invoice.cashDesk.deskCode", target = "cashDeskCode")
  @Mapping(source = "invoice.issuedByStaffId", target = "issuedByStaffId")
  @Mapping(source = "invoice.status", target = "status")
  @Mapping(source = "invoice.issuedAt", target = "issuedAt")
  @Mapping(source = "invoice.dueDate", target = "dueDate")
  @Mapping(source = "invoice.dateCreated", target = "dateCreated")
  @Mapping(source = "invoice.dateModified", target = "dateModified")
  @Mapping(source = "subtotal", target = "subtotal")
  @Mapping(source = "totalDiscount", target = "totalDiscount")
  @Mapping(source = "totalTax", target = "totalTax")
  @Mapping(source = "totalAmount", target = "totalAmount")
  @Mapping(source = "paidAmount", target = "paidAmount")
  @Mapping(source = "balanceDue", target = "balanceDue")
  @Mapping(source = "lineItems", target = "lineItems")
  @Mapping(source = "payments", target = "payments")
  @Mapping(source = "insuranceClaim", target = "insuranceClaim")
  InvoiceDetailResponseDTO toDetailResponseDTO(
      Invoice invoice,
      BigDecimal subtotal,
      BigDecimal totalDiscount,
      BigDecimal totalTax,
      BigDecimal totalAmount,
      BigDecimal paidAmount,
      BigDecimal balanceDue,
      List<InvoiceLineItemResponseDTO> lineItems,
      List<PaymentResponseDTO> payments,
      InsuranceClaimResponseDTO insuranceClaim);
}
