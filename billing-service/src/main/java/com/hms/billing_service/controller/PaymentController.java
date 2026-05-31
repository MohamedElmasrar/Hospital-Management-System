package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.payment.PaymentResponseDTO;
import com.hms.billing_service.dto.payment.ProcessPaymentRequestDTO;
import com.hms.billing_service.security.AuthenticatedPrincipal;
import com.hms.billing_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Process patient and third-party payments")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Process a payment against an invoice")
  public ResponseEntity<PaymentResponseDTO> processPayment(
      @Valid @RequestBody ProcessPaymentRequestDTO request,
      @Parameter(hidden = true) AuthenticatedPrincipal principal) {
    UUID cashierId =
        principal != null && principal.userId() != null ? principal.userId() : UUID.randomUUID();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(paymentService.processPayment(request, cashierId));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get payment by ID")
  public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable UUID id) {
    return ResponseEntity.ok(paymentService.getById(id));
  }

  @GetMapping("/invoice/{invoiceId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get all payments for an invoice")
  public ResponseEntity<List<PaymentResponseDTO>> getByInvoice(@PathVariable UUID invoiceId) {
    return ResponseEntity.ok(paymentService.getByInvoice(invoiceId));
  }

  @GetMapping("/cash-desk/{cashDeskId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get payments processed at a cash desk")
  public ResponseEntity<PageResponse<PaymentResponseDTO>> getByCashDesk(
      @PathVariable UUID cashDeskId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(paymentService.getByCashDesk(cashDeskId, pageable));
  }

  @PostMapping("/{id}/refund")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Refund a payment")
  public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable UUID id) {
    return ResponseEntity.ok(paymentService.refundPayment(id));
  }
}
