package com.hms.reception_service.controller;

import com.hms.reception_service.dto.reminder.ReminderDTO;
import com.hms.reception_service.service.ReminderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/reminders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'RECEPTIONIST')")
public class ReminderController {
  private final ReminderService reminderService;

  @PostMapping
  public ResponseEntity<ReminderDTO.Response> create(
      @Valid @RequestBody ReminderDTO.CreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.create(request));
  }

  @GetMapping("/reservation/{reservationId}")
  public ResponseEntity<List<ReminderDTO.Response>> findByReservation(
      @PathVariable UUID reservationId) {
    return ResponseEntity.ok(reminderService.findByReservation(reservationId));
  }

  @PostMapping("/{id}/sent")
  public ResponseEntity<ReminderDTO.Response> markSent(@PathVariable UUID id) {
    return ResponseEntity.ok(reminderService.markSent(id));
  }
}
