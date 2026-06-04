package com.hms.billing_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.billing_service.dto.invoice.CreateInvoiceLineItemRequestDTO;
import com.hms.billing_service.dto.invoice.CreateInvoiceRequestDTO;
import com.hms.billing_service.dto.invoice.InvoiceDetailResponseDTO;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.exception.GlobalExceptionHandler;
import com.hms.billing_service.security.SecurityConfig;
import com.hms.billing_service.service.InvoiceService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InvoiceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InvoiceControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private InvoiceService invoiceService;

  @Test
  void createInvoice_whenBiller_shouldReturnCreated() throws Exception {
    UUID patientId = UUID.randomUUID();
    UUID cashDeskId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    CreateInvoiceRequestDTO request =
        CreateInvoiceRequestDTO.builder()
            .patientId(patientId)
            .cashDeskId(cashDeskId)
            .lineItems(
                List.of(
                    CreateInvoiceLineItemRequestDTO.builder()
                        .billableOrderId(orderId)
                        .quantity(1)
                        .discountRate(BigDecimal.ZERO)
                        .build()))
            .build();

    InvoiceDetailResponseDTO responseDTO =
        InvoiceDetailResponseDTO.builder()
            .id(UUID.randomUUID())
            .invoiceNumber("INV-20261015-TEST1")
            .patientId(patientId)
            .cashDeskId(cashDeskId)
            .cashDeskCode("DESK-01")
            .issuedByStaffId(UUID.randomUUID())
            .status(InvoiceStatus.DRAFT)
            .subtotal(new BigDecimal("100.00"))
            .totalDiscount(BigDecimal.ZERO)
            .totalTax(BigDecimal.ZERO)
            .totalAmount(new BigDecimal("100.00"))
            .paidAmount(BigDecimal.ZERO)
            .balanceDue(new BigDecimal("100.00"))
            .issuedAt(new Date())
            .lineItems(Collections.emptyList())
            .payments(Collections.emptyList())
            .dateCreated(new Date())
            .dateModified(new Date())
            .build();

    when(invoiceService.create(any(CreateInvoiceRequestDTO.class), any(UUID.class)))
        .thenReturn(responseDTO);

    mockMvc
        .perform(
            post("/invoices")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_BILLER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void createInvoice_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
    CreateInvoiceRequestDTO request =
        CreateInvoiceRequestDTO.builder()
            .patientId(UUID.randomUUID())
            .cashDeskId(UUID.randomUUID())
            .lineItems(
                List.of(
                    CreateInvoiceLineItemRequestDTO.builder()
                        .billableOrderId(UUID.randomUUID())
                        .quantity(1)
                        .discountRate(BigDecimal.ZERO)
                        .build()))
            .build();

    mockMvc
        .perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createInvoice_whenDoctor_shouldReturnForbidden() throws Exception {
    CreateInvoiceRequestDTO request =
        CreateInvoiceRequestDTO.builder()
            .patientId(UUID.randomUUID())
            .cashDeskId(UUID.randomUUID())
            .lineItems(
                List.of(
                    CreateInvoiceLineItemRequestDTO.builder()
                        .billableOrderId(UUID.randomUUID())
                        .quantity(1)
                        .discountRate(BigDecimal.ZERO)
                        .build()))
            .build();

    mockMvc
        .perform(
            post("/invoices")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void voidInvoice_whenAdmin_shouldSucceed() throws Exception {
    UUID invoiceId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/invoices/{id}/void", invoiceId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
  }

  @Test
  void voidInvoice_whenBiller_shouldReturnForbidden() throws Exception {
    UUID invoiceId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/invoices/{id}/void", invoiceId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_BILLER"))))
        .andExpect(status().isForbidden());
  }
}
