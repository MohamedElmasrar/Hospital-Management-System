package com.hms.reception_service.controller;

import com.hms.reception_service.dto.prescription.DiagnosticPrescriptionDTO;
import com.hms.reception_service.security.AuthenticatedPrincipal;
import com.hms.reception_service.service.DiagnosticPrescriptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/prescriptions")
@RequiredArgsConstructor
public class DiagnosticPrescriptionController {

  private final DiagnosticPrescriptionService prescriptionService;

  @PostMapping
  @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
  public ResponseEntity<DiagnosticPrescriptionDTO.Response> createPrescription(
      @Valid @RequestBody DiagnosticPrescriptionDTO.CreateRequest req,
      AuthenticatedPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(prescriptionService.createPrescription(req, principal.userId()));
  }

  @GetMapping("/patient/{patientId}/pending")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<List<DiagnosticPrescriptionDTO.Response>> getPendingPrescriptions(
      @PathVariable UUID patientId) {
    return ResponseEntity.ok(prescriptionService.getPendingPrescriptionsByPatient(patientId));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<DiagnosticPrescriptionDTO.Response> getPrescriptionById(
      @PathVariable UUID id) {
    return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
  }
}
