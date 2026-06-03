package com.hms.billing_service.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hms.billing_service.dto.invoice.CreateInvoiceLineItemRequestDTO;
import com.hms.billing_service.dto.invoice.CreateInvoiceRequestDTO;
import com.hms.billing_service.dto.invoice.InvoiceDetailResponseDTO;
import com.hms.billing_service.dto.order.CreateBillableOrderRequestDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.enums.SourceOrderType;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.repository.BillableOrderRepository;
import com.hms.billing_service.repository.InvoiceLineItemRepository;
import com.hms.billing_service.repository.ServiceCatalogRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ConsultationBillingService {
  private final BillableOrderRepository orderRepository;
  private final InvoiceLineItemRepository lineItemRepository;
  private final BillableOrderService orderService;
  private final InvoiceService invoiceService;
  private final ServiceCatalogRepository catalogRepository;

  @Value("${reception.service.url:http://reception-service:8004}")
  private String receptionUrl;

  @Transactional
  public InvoiceDetailResponseDTO prepare(
      UUID reservationId, UUID serviceId, UUID cashDeskId, UUID staffId) {
    var authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    var consultation =
        RestClient.create(receptionUrl)
            .get()
            .uri("/api/reception/reservations/{id}", reservationId)
            .headers(headers -> headers.setBearerAuth(authentication.getToken().getTokenValue()))
            .retrieve()
            .body(Consultation.class);
    if (consultation == null
        || !"CONSULTATION".equals(consultation.categoryCode())
        || !"PENDING_PAYMENT".equals(consultation.statusCode())
        || Boolean.TRUE.equals(consultation.isPaid())) {
      throw new InvalidOperationException("Only consultations awaiting payment can be billed");
    }
    var catalog =
        catalogRepository
            .findById(serviceId)
            .orElseThrow(() -> new InvalidOperationException("Service tariff not found"));
    if (!Boolean.TRUE.equals(catalog.getIsActive())
        || !consultation.departmentId().equals(catalog.getDepartmentId())) {
      throw new InvalidOperationException(
          "Choose an active service in the consultation's department");
    }
    var order =
        orderRepository.findByExternalOrderIdAndOrderType(
            reservationId, SourceOrderType.APPOINTMENT);
    UUID orderId =
        order.isPresent()
            ? order.get().getId()
            : orderService
                .create(
                    new CreateBillableOrderRequestDTO(
                        reservationId,
                        SourceOrderType.APPOINTMENT,
                        consultation.patientId(),
                        consultation.practitionerId(),
                        consultation.departmentId(),
                        null,
                        serviceId,
                        null,
                        null))
                .id();
    var existing = lineItemRepository.findByBillableOrderId(orderId);
    if (existing.isPresent()) {
      var invoice = existing.get().getInvoice();
      if (invoice.getStatus() == InvoiceStatus.VOID
          || invoice.getStatus() == InvoiceStatus.REFUNDED) {
        throw new InvalidOperationException(
            "This consultation has a void or refunded invoice; review it before billing again");
      }
      return invoiceService.getById(invoice.getId());
    }
    var invoice =
        invoiceService.create(
            new CreateInvoiceRequestDTO(
                consultation.patientId(),
                cashDeskId,
                null,
                List.of(new CreateInvoiceLineItemRequestDTO(orderId, 1, BigDecimal.ZERO))),
            staffId);
    return invoiceService.updateStatus(invoice.id(), InvoiceStatus.ISSUED);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Consultation(
      UUID patientId,
      UUID practitionerId,
      UUID departmentId,
      String categoryCode,
      String statusCode,
      Boolean isPaid) {}
}
