package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.insurance.*;
import com.hms.billing_service.enums.ClaimStatus;
import com.hms.billing_service.service.InsuranceService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/insurance")
@RequiredArgsConstructor
@Tag(
    name = "Insurance & Claims",
    description = "Manage insurance providers, patient policies, and claims")
public class InsuranceController {

  private final InsuranceService insuranceService;

  @PostMapping("/providers")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create an insurance provider")
  public ResponseEntity<InsuranceProviderResponseDTO> createProvider(
      @Valid @RequestBody CreateInsuranceProviderRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.createProvider(request));
  }

  @GetMapping("/providers/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get insurance provider by ID")
  public ResponseEntity<InsuranceProviderResponseDTO> getProvider(@PathVariable UUID id) {
    return ResponseEntity.ok(insuranceService.getProviderById(id));
  }

  @GetMapping("/providers")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "List all insurance providers")
  public ResponseEntity<List<InsuranceProviderResponseDTO>> listProviders() {
    return ResponseEntity.ok(insuranceService.getAllProviders());
  }

  @PostMapping("/policies")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Register patient insurance policy")
  public ResponseEntity<InsurancePolicyResponseDTO> createPolicy(
      @Valid @RequestBody CreateInsurancePolicyRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.createPolicy(request));
  }

  @GetMapping("/policies/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get insurance policy by ID")
  public ResponseEntity<InsurancePolicyResponseDTO> getPolicy(@PathVariable UUID id) {
    return ResponseEntity.ok(insuranceService.getPolicyById(id));
  }

  @GetMapping("/policies/patient/{patientId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get insurance policies for a patient")
  public ResponseEntity<List<InsurancePolicyResponseDTO>> getPoliciesByPatient(
      @PathVariable UUID patientId) {
    return ResponseEntity.ok(insuranceService.getPoliciesByPatient(patientId));
  }

  @PostMapping("/claims")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Submit insurance claim for an invoice")
  public ResponseEntity<InsuranceClaimResponseDTO> submitClaim(
      @Valid @RequestBody SubmitClaimRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.submitClaim(request));
  }

  @PutMapping("/claims/{id}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Update claim status (APPROVED, REJECTED, SETTLED)")
  public ResponseEntity<InsuranceClaimResponseDTO> updateClaimStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateClaimStatusRequestDTO request) {
    return ResponseEntity.ok(insuranceService.updateClaimStatus(id, request));
  }

  @GetMapping("/claims/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get insurance claim by ID")
  public ResponseEntity<InsuranceClaimResponseDTO> getClaim(@PathVariable UUID id) {
    return ResponseEntity.ok(insuranceService.getClaimById(id));
  }

  @GetMapping("/claims")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "List insurance claims by status")
  public ResponseEntity<PageResponse<InsuranceClaimResponseDTO>> listClaims(
      @RequestParam ClaimStatus status,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(insuranceService.getClaimsByStatus(status, pageable));
  }
}
