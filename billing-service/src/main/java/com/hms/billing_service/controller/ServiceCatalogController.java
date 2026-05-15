package com.hms.billing_service.controller;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.catalog.*;
import com.hms.billing_service.service.ServiceCatalogService;
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
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(
    name = "Service Catalog & Tariffs",
    description = "Manage hospital services and versioned tariffs")
public class ServiceCatalogController {

  private final ServiceCatalogService serviceCatalogService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a service with initial tariff")
  public ResponseEntity<ServiceCatalogResponseDTO> createService(
      @Valid @RequestBody CreateServiceCatalogRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogService.create(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update service catalog details")
  public ResponseEntity<ServiceCatalogResponseDTO> updateService(
      @PathVariable UUID id, @Valid @RequestBody UpdateServiceCatalogRequestDTO request) {
    return ResponseEntity.ok(serviceCatalogService.update(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get service catalog item by ID with current tariff")
  public ResponseEntity<ServiceCatalogResponseDTO> getService(@PathVariable UUID id) {
    return ResponseEntity.ok(serviceCatalogService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "List services with optional department filter")
  public ResponseEntity<PageResponse<ServiceCatalogResponseDTO>> listServices(
      @RequestParam(required = false) UUID departmentId,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    if (departmentId != null) {
      return ResponseEntity.ok(serviceCatalogService.getByDepartment(departmentId, pageable));
    }
    return ResponseEntity.ok(serviceCatalogService.getAll(pageable));
  }

  @PostMapping("/{id}/tariffs")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add a new versioned tariff (automatically closes the previous version)")
  public ResponseEntity<TariffVersionResponseDTO> addTariffVersion(
      @PathVariable UUID id, @Valid @RequestBody CreateTariffVersionRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(serviceCatalogService.addTariffVersion(id, request));
  }

  @GetMapping("/{id}/tariffs")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get tariff version history for a service")
  public ResponseEntity<List<TariffVersionResponseDTO>> getTariffHistory(@PathVariable UUID id) {
    return ResponseEntity.ok(serviceCatalogService.getTariffHistory(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Deactivate service catalog item")
  public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
    serviceCatalogService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
