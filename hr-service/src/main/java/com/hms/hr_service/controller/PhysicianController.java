package com.hms.hr_service.controller;

import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.Physician.AssignPhysicianSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.CreatePhysicianRequestDTO;
import com.hms.hr_service.dto.Physician.PhysicianResponseDTO;
import com.hms.hr_service.dto.Physician.UpdatePhysicianRequestDTO;
import com.hms.hr_service.service.PhysicianService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/physicians")
@RequiredArgsConstructor
public class PhysicianController {

  private final PhysicianService physicianService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Register an employee as a physician")
  public ResponseEntity<PhysicianResponseDTO> createPhysician(
      @Valid @RequestBody CreatePhysicianRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(physicianService.create(request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get physician details and specialties")
  public ResponseEntity<PhysicianResponseDTO> getPhysician(@PathVariable UUID id) {
    return ResponseEntity.ok(physicianService.getById(id));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Search physicians with pagination")
  public ResponseEntity<PageResponse<PhysicianResponseDTO>> searchPhysicians(
      @RequestParam(required = false) String search,
      @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(physicianService.search(search, pageable));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Update physician license number")
  public ResponseEntity<PhysicianResponseDTO> updatePhysician(
      @PathVariable UUID id, @Valid @RequestBody UpdatePhysicianRequestDTO request) {
    return ResponseEntity.ok(physicianService.update(id, request));
  }

  @PostMapping("/{id}/specialties")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Assign a medical specialty to a physician")
  public ResponseEntity<PhysicianResponseDTO> assignSpecialty(
      @PathVariable UUID id, @Valid @RequestBody AssignPhysicianSpecialtyRequestDTO request) {
    return ResponseEntity.ok(physicianService.assignSpecialty(id, request));
  }

  @DeleteMapping("/{id}/specialties/{specialtyId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Remove a medical specialty from a physician")
  public ResponseEntity<Void> removeSpecialty(
      @PathVariable UUID id, @PathVariable UUID specialtyId) {
    physicianService.removeSpecialty(id, specialtyId);
    return ResponseEntity.noContent().build();
  }
}
