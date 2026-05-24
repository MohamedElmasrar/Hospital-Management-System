package com.hms.reception_service.controller;

import com.hms.reception_service.dto.admission.AdmissionDTO;
import com.hms.reception_service.dto.admission.EmergencyAdmissionDTO;
import com.hms.reception_service.service.AdmissionService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/admissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST')")
public class AdmissionController {
  private final AdmissionService admissionService;

  @PostMapping("/beds")
  public ResponseEntity<AdmissionDTO.Response> createBed(
      @Valid @RequestBody AdmissionDTO.CreateBedRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.createBed(request));
  }

  @GetMapping("/beds/available")
  public ResponseEntity<List<AdmissionDTO.Response>> availableBeds() {
    return ResponseEntity.ok(admissionService.availableBeds());
  }

  @PostMapping
  public ResponseEntity<AdmissionDTO.Response> admit(
      @Valid @RequestBody AdmissionDTO.AdmitRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.admit(request));
  }

  @PostMapping("/{id}/discharge")
  public ResponseEntity<AdmissionDTO.Response> discharge(@PathVariable UUID id) {
    return ResponseEntity.ok(admissionService.discharge(id));
  }

  @PostMapping("/emergency")
  public ResponseEntity<EmergencyAdmissionDTO.Response> emergency(
      @Valid @RequestBody EmergencyAdmissionDTO.CreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.emergency(request));
  }
}
