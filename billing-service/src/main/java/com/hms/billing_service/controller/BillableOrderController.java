package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.order.BillableOrderResponseDTO;
import com.hms.billing_service.dto.order.CreateBillableOrderRequestDTO;
import com.hms.billing_service.service.BillableOrderService;
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
@RequestMapping("/billable-orders")
@RequiredArgsConstructor
@Tag(
    name = "Billable Clinical Orders",
    description = "Register billable appointments and diagnostic orders")
public class BillableOrderController {

  private final BillableOrderService billableOrderService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'DOCTOR')")
  @Operation(summary = "Register a billable clinical order")
  public ResponseEntity<BillableOrderResponseDTO> createOrder(
      @Valid @RequestBody CreateBillableOrderRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(billableOrderService.create(request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'DOCTOR')")
  @Operation(summary = "Get billable order by ID")
  public ResponseEntity<BillableOrderResponseDTO> getOrder(@PathVariable UUID id) {
    return ResponseEntity.ok(billableOrderService.getById(id));
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'DOCTOR')")
  @Operation(summary = "Get billable orders for a patient")
  public ResponseEntity<PageResponse<BillableOrderResponseDTO>> getByPatient(
      @PathVariable UUID patientId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(billableOrderService.getByPatient(patientId, pageable));
  }

  @GetMapping("/practitioner/{practitionerId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'DOCTOR')")
  @Operation(summary = "Get billable orders for a practitioner")
  public ResponseEntity<PageResponse<BillableOrderResponseDTO>> getByPractitioner(
      @PathVariable UUID practitionerId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(billableOrderService.getByPractitioner(practitionerId, pageable));
  }
}
