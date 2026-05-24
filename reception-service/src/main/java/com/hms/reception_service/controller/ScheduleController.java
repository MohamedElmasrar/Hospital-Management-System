package com.hms.reception_service.controller;

import com.hms.reception_service.dto.schedule.ScheduleDTO;
import com.hms.reception_service.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/schedules")
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ScheduleDTO.Response> createSchedule(
      @Valid @RequestBody ScheduleDTO.CreateRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(req));
  }

  @GetMapping("/department/{departmentId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByDepartment(
      @PathVariable UUID departmentId) {
    return ResponseEntity.ok(scheduleService.getSchedulesByDepartment(departmentId));
  }

  @GetMapping("/practitioner/{practitionerId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public ResponseEntity<List<ScheduleDTO.Response>> getSchedulesByPractitioner(
      @PathVariable UUID practitionerId) {
    return ResponseEntity.ok(scheduleService.getSchedulesByPractitioner(practitionerId));
  }

  @GetMapping("/available-slots")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
  public ResponseEntity<List<ScheduleDTO.AvailableSlot>> getAvailableSlots(
      @RequestParam(required = false) UUID practitionerId,
      @RequestParam(required = false) UUID roomId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(scheduleService.getAvailableSlots(practitionerId, roomId, date));
  }
}
