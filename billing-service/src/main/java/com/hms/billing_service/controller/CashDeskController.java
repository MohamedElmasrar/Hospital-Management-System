package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.cashdesk.CashDeskResponseDTO;
import com.hms.billing_service.dto.cashdesk.CreateCashDeskRequestDTO;
import com.hms.billing_service.dto.cashdesk.UpdateCashDeskRequestDTO;
import com.hms.billing_service.service.CashDeskService;
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
@RequestMapping("/cash-desks")
@RequiredArgsConstructor
@Tag(name = "Cash Desks", description = "Manage physical and virtual cash desks")
public class CashDeskController {

  private final CashDeskService cashDeskService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a cash desk")
  public ResponseEntity<CashDeskResponseDTO> createCashDesk(
      @Valid @RequestBody CreateCashDeskRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(cashDeskService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update cash desk details")
  public ResponseEntity<CashDeskResponseDTO> updateCashDesk(
      @PathVariable UUID id, @Valid @RequestBody UpdateCashDeskRequestDTO request) {
    return ResponseEntity.ok(cashDeskService.update(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get cash desk by ID")
  public ResponseEntity<CashDeskResponseDTO> getCashDesk(@PathVariable UUID id) {
    return ResponseEntity.ok(cashDeskService.getById(id));
  }

  @GetMapping("/code/{deskCode}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "Get cash desk by code")
  public ResponseEntity<CashDeskResponseDTO> getByDeskCode(@PathVariable String deskCode) {
    return ResponseEntity.ok(cashDeskService.getByDeskCode(deskCode));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  @Operation(summary = "List cash desks with optional department filter")
  public ResponseEntity<PageResponse<CashDeskResponseDTO>> listCashDesks(
      @RequestParam(required = false) UUID departmentId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    if (departmentId != null) {
      return ResponseEntity.ok(cashDeskService.getByDepartment(departmentId, pageable));
    }
    return ResponseEntity.ok(cashDeskService.getAll(pageable));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Deactivate cash desk")
  public ResponseEntity<Void> deleteCashDesk(@PathVariable UUID id) {
    cashDeskService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
