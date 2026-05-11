package com.hms.hr_service.controller;

import com.hms.hr_service.dto.Physician.CreateMedicalSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.MedicalSpecialtyResponseDTO;
import com.hms.hr_service.service.PhysicianService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medical-specialties")
@RequiredArgsConstructor
public class MedicalSpecialtyController {

  private final PhysicianService physicianService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD')")
  @Operation(summary = "Create a new medical specialty")
  public ResponseEntity<MedicalSpecialtyResponseDTO> createSpecialty(
      @Valid @RequestBody CreateMedicalSpecialtyRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(physicianService.createSpecialty(request));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "List all medical specialties")
  public ResponseEntity<List<MedicalSpecialtyResponseDTO>> listSpecialties() {
    return ResponseEntity.ok(physicianService.getAllSpecialties());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'HR_HEAD', 'EMPLOYEE', 'DOCTOR')")
  @Operation(summary = "Get medical specialty by ID")
  public ResponseEntity<MedicalSpecialtyResponseDTO> getSpecialty(@PathVariable UUID id) {
    return ResponseEntity.ok(physicianService.getSpecialtyById(id));
  }
}
