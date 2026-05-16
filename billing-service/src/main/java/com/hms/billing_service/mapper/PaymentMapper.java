package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.model.Payment;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

  @Mapping(source = "invoice.id", target = "invoiceId")
  @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "cashDesk.id", target = "cashDeskId")
  @Mapping(source = "cashDesk.deskCode", target = "cashDeskCode")
  PaymentResponseDTO paymentToPaymentResponseDTO(Payment payment);

  List<PaymentResponseDTO> toPaymentResponseDTOList(List<Payment> payments);
}
