package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.invoice.*;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.security.AuthenticatedPrincipal;
import com.hms.billing_service.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(
    name = "Invoices",
    description = "Manage patient invoices, line items, and financial calculations")
public class InvoiceController {

  private final InvoiceService invoiceService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Create an invoice")
  public ResponseEntity<InvoiceDetailResponseDTO> createInvoice(
      @Valid @RequestBody CreateInvoiceRequestDTO request, AuthenticatedPrincipal principal) {
    UUID staffId =
        principal != null && principal.userId() != null ? principal.userId() : UUID.randomUUID();
    return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request, staffId));
  }

  @PostMapping("/automatic")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Create an invoice from all unbilled clinical charges")
  public ResponseEntity<InvoiceDetailResponseDTO> createAutomaticInvoice(
      @Valid @RequestBody CreateAutomaticInvoiceRequestDTO request,
      AuthenticatedPrincipal principal) {
    UUID staffId =
        principal != null && principal.userId() != null ? principal.userId() : UUID.randomUUID();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(invoiceService.createAutomatic(request, staffId));
  }

  @PostMapping("/{id}/line-items")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Add line item to invoice (DRAFT only)")
  public ResponseEntity<InvoiceDetailResponseDTO> addLineItem(
      @PathVariable UUID id, @Valid @RequestBody AddLineItemRequestDTO request) {
    return ResponseEntity.ok(invoiceService.addLineItem(id, request));
  }

  @DeleteMapping("/{id}/line-items/{lineItemId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Remove line item from invoice (DRAFT only)")
  public ResponseEntity<InvoiceDetailResponseDTO> removeLineItem(
      @PathVariable UUID id, @PathVariable UUID lineItemId) {
    return ResponseEntity.ok(invoiceService.removeLineItem(id, lineItemId));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Update invoice status")
  public ResponseEntity<InvoiceDetailResponseDTO> updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateInvoiceStatusRequestDTO request) {
    return ResponseEntity.ok(invoiceService.updateStatus(id, request.status()));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get full invoice details by ID")
  public ResponseEntity<InvoiceDetailResponseDTO> getInvoice(@PathVariable UUID id) {
    return ResponseEntity.ok(invoiceService.getById(id));
  }

  @GetMapping(value = "/{id}/receipt", produces = "text/plain")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get a printable invoice receipt")
  public ResponseEntity<String> getReceipt(@PathVariable UUID id) {
    InvoiceDetailResponseDTO invoice = invoiceService.getById(id);
    String receipt =
        "Invoice: "
            + invoice.invoiceNumber()
            + "\nPatient: "
            + invoice.patientId()
            + "\nStatus: "
            + invoice.status()
            + "\nTotal: "
            + invoice.totalAmount()
            + "\nPaid: "
            + invoice.paidAmount()
            + "\nBalance: "
            + invoice.balanceDue();
    return ResponseEntity.ok(receipt);
  }

  @GetMapping("/number/{invoiceNumber}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get full invoice details by invoice number")
  public ResponseEntity<InvoiceDetailResponseDTO> getInvoiceByNumber(
      @PathVariable String invoiceNumber) {
    return ResponseEntity.ok(invoiceService.getByInvoiceNumber(invoiceNumber));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "List invoices with optional filters")
  public ResponseEntity<PageResponse<InvoiceSummaryResponseDTO>> listInvoices(
      @RequestParam(required = false) UUID patientId,
      @RequestParam(required = false) InvoiceStatus status,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(invoiceService.search(patientId, status, pageable));
  }

  @PostMapping("/{id}/void")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Void invoice")
  public ResponseEntity<Void> voidInvoice(@PathVariable UUID id) {
    invoiceService.voidInvoice(id);
    return ResponseEntity.noContent().build();
  }
}
