package com.hms.reception_service.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.hms.reception_service.exception.InvalidOperationException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BillingPaymentVerifierTest {
  private final UUID patientId = UUID.randomUUID();
  private final UUID invoiceId = UUID.randomUUID();
  private final UUID reservationId = UUID.randomUUID();
  private final UUID orderId = UUID.randomUUID();
  private BillingPaymentVerifier verifier;
  private MockRestServiceServer server;

  @BeforeEach
  void setUp() {
    var builder = RestClient.builder().baseUrl("http://billing");
    server = MockRestServiceServer.bindTo(builder).build();
    verifier = new BillingPaymentVerifier("http://billing");
    ReflectionTestUtils.setField(verifier, "client", builder.build());
    var jwt = Jwt.withTokenValue("biller-token").header("alg", "HS256").subject("biller").build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void acceptsFullyPaidInvoiceLinkedToTheConsultation() {
    expectInvoice("PAID", "0");
    server
        .expect(requestTo("http://billing/billable-orders/" + orderId))
        .andRespond(
            withSuccess(
                "{\"externalOrderId\":\"" + reservationId + "\",\"orderType\":\"APPOINTMENT\"}",
                MediaType.APPLICATION_JSON));
    verifier.requirePaidInvoice(invoiceId, patientId, reservationId);
    server.verify();
  }

  @Test
  void refusesApprovalWhenBalanceIsOutstanding() {
    expectInvoice("ISSUED", "100");
    assertThatThrownBy(() -> verifier.requirePaidInvoice(invoiceId, patientId, reservationId))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("fully paid");
    server.verify();
  }

  @Test
  void refusesPaidInvoiceForAnotherConsultation() {
    expectInvoice("PAID", "0");
    server
        .expect(requestTo("http://billing/billable-orders/" + orderId))
        .andRespond(
            withSuccess(
                "{\"externalOrderId\":\"" + UUID.randomUUID() + "\",\"orderType\":\"APPOINTMENT\"}",
                MediaType.APPLICATION_JSON));
    assertThatThrownBy(() -> verifier.requirePaidInvoice(invoiceId, patientId, reservationId))
        .isInstanceOf(InvalidOperationException.class)
        .hasMessageContaining("does not cover");
    server.verify();
  }

  private void expectInvoice(String status, String balance) {
    server
        .expect(requestTo("http://billing/invoices/" + invoiceId))
        .andExpect(header("Authorization", "Bearer biller-token"))
        .andRespond(
            withSuccess(
                "{\"patientId\":\""
                    + patientId
                    + "\",\"status\":\""
                    + status
                    + "\",\"balanceDue\":"
                    + balance
                    + ",\"lineItems\":[{\"billableOrderId\":\""
                    + orderId
                    + "\"}]}",
                MediaType.APPLICATION_JSON));
  }
}
