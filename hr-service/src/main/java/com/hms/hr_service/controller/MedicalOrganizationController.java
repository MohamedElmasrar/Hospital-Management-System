package com.hms.hr_service.controller;

import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.GetMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.UpdateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.service.MedicalOrganizationService;
import com.hms.hr_service.validation.AllowedSortFields;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/medical-organizations")
public class MedicalOrganizationController {

  private final MedicalOrganizationService medicalOrganizationService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  @Operation(
      summary =
          "Endpoint to get all the Medical Organizations based on a search term sorted by name")
  public ResponseEntity<PageResponse<MedicalOrganizationResponseDTO>> listMedicalOrganizations(
      @ParameterObject @Valid @ModelAttribute GetMedicalOrganizationRequestDTO filter,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
      @ParameterObject @PageableDefault(size = 20) @AllowedSortFields({"name"}) Pageable pageable) {
    return ResponseEntity.ok(medicalOrganizationService.search(filter, pageable));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Endpoint to create a new Medical Organization")
  public ResponseEntity<CreateMedicalOrganizationResponseDTO> createMedicalOrganization(
      @Valid @RequestBody CreateMedicalOrganizationRequestDTO medicalOrganizationRequestDTO,
      AuthenticatedPrincipal principal) {
    return ResponseEntity.ok(
        medicalOrganizationService.create(medicalOrganizationRequestDTO, principal));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Endpoint for updating a Medical Organization")
  public ResponseEntity<Void> updateMedicalOrganization(
      @PathVariable @NotBlank @UUID String id,
      @RequestBody @Valid UpdateMedicalOrganizationRequestDTO updateMedicalOrganizationRequestDTO,
      AuthenticatedPrincipal principal) {

    medicalOrganizationService.update(id, updateMedicalOrganizationRequestDTO, principal);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Endpoint for delete a Medical Organization by Id")
  public ResponseEntity<Void> deleteMedicalOrganization(
      @PathVariable @NotBlank @UUID String id, AuthenticatedPrincipal principal) {
    medicalOrganizationService.delete(id, principal);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Endpoint to get the details of a specific Medical Organization")
  public ResponseEntity<MedicalOrganizationResponseDTO> getDetailsOfMedicalOrganization(
      @PathVariable @NotBlank @UUID String id) {
    return ResponseEntity.ok(medicalOrganizationService.getById(id));
  }
}
