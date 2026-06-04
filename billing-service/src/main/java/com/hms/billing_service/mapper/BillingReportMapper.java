package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.report.UnpaidInvoiceResponseDTO;
import com.hms.billing_service.model.Invoice;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BillingReportMapper {

  @Mapping(source = "invoice.id", target = "invoiceId")
  @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "invoice.patientId", target = "patientId")
  @Mapping(source = "totalAmount", target = "totalAmount")
  @Mapping(source = "paidAmount", target = "paidAmount")
  @Mapping(source = "balanceDue", target = "balanceDue")
  @Mapping(source = "invoice.dueDate", target = "dueDate")
  UnpaidInvoiceResponseDTO toUnpaidInvoiceResponseDTO(
      Invoice invoice, BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal balanceDue);
}
