package com.hms.reception_service.controller;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.service.PatientService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/patients")
@RequiredArgsConstructor
public class PatientController {

  private final PatientService patientService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST')")
  public ResponseEntity<PatientDTO.Response> createPatient(
      @Valid @RequestBody PatientDTO.CreateRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(req));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<PatientDTO.Response> getPatientById(@PathVariable UUID id) {
    return ResponseEntity.ok(patientService.getPatientById(id));
  }

  @GetMapping("/mrn/{mrn}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<PatientDTO.Response> getPatientByMrn(@PathVariable String mrn) {
    return ResponseEntity.ok(patientService.getPatientByMrn(mrn));
  }

  @GetMapping("/practitioner/{practitionerId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
  public ResponseEntity<PageResponse<PatientDTO.Response>> searchPractitionerPatients(
      @PathVariable UUID practitionerId,
      @RequestParam(required = false) String query,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(
        patientService.searchPatientsByPractitioner(practitionerId, query, pageable));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<PageResponse<PatientDTO.Response>> searchPatients(
      @RequestParam(required = false) String query, @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(patientService.searchPatients(query, pageable));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST')")
  public ResponseEntity<PatientDTO.Response> updatePatient(
      @PathVariable UUID id, @Valid @RequestBody PatientDTO.UpdateRequest req) {
    return ResponseEntity.ok(patientService.updatePatient(id, req));
  }
}
