package com.hms.reception_service.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hms.reception_service.exception.InvalidOperationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BillingPaymentVerifier {
  private final RestClient client;

  public BillingPaymentVerifier(
      @Value("${billing.service.url:http://billing-service:8003}") String billingUrl) {
    client = RestClient.create(billingUrl);
  }

  public void requirePaidInvoice(UUID invoiceId, UUID patientId, UUID reservationId) {
    if (invoiceId == null) throw new InvalidOperationException("A paid invoice is required");
    var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    String token = auth.getToken().getTokenValue();
    var invoice =
        client
            .get()
            .uri("/invoices/{id}", invoiceId)
            .headers(headers -> headers.setBearerAuth(token))
            .retrieve()
            .body(PaidInvoice.class);
    if (invoice == null
        || !patientId.equals(invoice.patientId())
        || !"PAID".equals(invoice.status())
        || invoice.balanceDue() == null
        || invoice.balanceDue().signum() > 0) {
      throw new InvalidOperationException(
          "The patient's invoice must be fully paid before approval");
    }
    for (var line : invoice.lineItems()) {
      var order =
          client
              .get()
              .uri("/billable-orders/{id}", line.billableOrderId())
              .headers(headers -> headers.setBearerAuth(token))
              .retrieve()
              .body(PaidOrder.class);
      if (order != null
          && reservationId.equals(order.externalOrderId())
          && "APPOINTMENT".equals(order.orderType())) return;
    }
    throw new InvalidOperationException("This invoice does not cover the consultation");
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PaidInvoice(
      UUID patientId, String status, BigDecimal balanceDue, List<PaidLine> lineItems) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PaidLine(UUID billableOrderId) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PaidOrder(UUID externalOrderId, String orderType) {}
}
