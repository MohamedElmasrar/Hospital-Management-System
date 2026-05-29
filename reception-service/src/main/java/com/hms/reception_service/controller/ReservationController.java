package com.hms.reception_service.controller;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.security.AuthenticatedPrincipal;
import com.hms.reception_service.service.ReservationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<PageResponse<ReservationDTO.Response>> searchReservations(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    var pageable =
        PageRequest.of(
            page,
            size,
            Sort.by("reservationDate").descending().and(Sort.by("startTime").descending()));
    return ResponseEntity.ok(reservationService.searchReservations(query, date, pageable));
  }

  @PostMapping("/consultation")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<ReservationDTO.Response> createConsultationReservation(
      @Valid @RequestBody ReservationDTO.CreateConsultationRequest req,
      AuthenticatedPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(reservationService.createConsultationReservation(req, principal.userId()));
  }

  @PostMapping("/diagnostic")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<ReservationDTO.Response> createDiagnosticReservation(
      @Valid @RequestBody ReservationDTO.CreateDiagnosticRequest req,
      AuthenticatedPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(reservationService.createDiagnosticReservation(req, principal.userId()));
  }

  @PostMapping("/{id}/pay")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST')")
  public ResponseEntity<ReservationDTO.Response> markAsPaid(
      @PathVariable UUID id, @RequestParam(required = false) UUID invoiceId) {
    return ResponseEntity.ok(reservationService.markAsPaid(id, invoiceId));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<ReservationDTO.Response> updateStatus(
      @PathVariable UUID id, @Valid @RequestBody ReservationDTO.UpdateStatusRequest req) {
    return ResponseEntity.ok(reservationService.updateStatus(id, req));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<ReservationDTO.Response> getReservationById(@PathVariable UUID id) {
    return ResponseEntity.ok(reservationService.getReservationById(id));
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<PageResponse<ReservationDTO.Response>> getReservationsByPatient(
      @PathVariable UUID patientId, @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(reservationService.getReservationsByPatient(patientId, pageable));
  }
}
