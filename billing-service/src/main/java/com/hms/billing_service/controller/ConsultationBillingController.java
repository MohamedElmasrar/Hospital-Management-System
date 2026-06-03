package com.hms.billing_service.controller;

import com.hms.billing_service.dto.invoice.InvoiceDetailResponseDTO;
import com.hms.billing_service.dto.invoice.PrepareConsultationInvoiceRequestDTO;
import com.hms.billing_service.security.AuthenticatedPrincipal;
import com.hms.billing_service.service.ConsultationBillingService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
public class ConsultationBillingController {
  private final ConsultationBillingService service;

  @PostMapping("/{reservationId}/invoice")
  public InvoiceDetailResponseDTO prepareConsultationInvoice(
      @PathVariable UUID reservationId,
      @Valid @RequestBody PrepareConsultationInvoiceRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    return service.prepare(
        reservationId, request.serviceCatalogId(), request.cashDeskId(), principal.userId());
  }
}
