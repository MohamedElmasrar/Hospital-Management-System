package com.hms.reception_service.controller;

import com.hms.reception_service.dto.catalog.*;
import com.hms.reception_service.service.AdminCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

  private final AdminCatalogService catalogService;

  // Categories
  @GetMapping("/categories")
  public ResponseEntity<List<ReservationCategoryDTO.Response>> getCategories() {
    return ResponseEntity.ok(catalogService.getAllCategories());
  }

  @PostMapping("/categories")
  public ResponseEntity<ReservationCategoryDTO.Response> createCategory(
      @Valid @RequestBody ReservationCategoryDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCategory(req));
  }

  // Modalities
  @GetMapping("/modalities")
  public ResponseEntity<List<DiagnosticModalityDTO.Response>> getModalities() {
    return ResponseEntity.ok(catalogService.getAllModalities());
  }

  @PostMapping("/modalities")
  public ResponseEntity<DiagnosticModalityDTO.Response> createModality(
      @Valid @RequestBody DiagnosticModalityDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createModality(req));
  }

  // Examination Rooms
  @GetMapping("/rooms")
  public ResponseEntity<List<ExaminationRoomDTO.Response>> getRooms() {
    return ResponseEntity.ok(catalogService.getAllRooms());
  }

  @PostMapping("/rooms")
  public ResponseEntity<ExaminationRoomDTO.Response> createRoom(
      @Valid @RequestBody ExaminationRoomDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createRoom(req));
  }

  // Priority Levels
  @GetMapping("/priorities")
  public ResponseEntity<List<QueuePriorityLevelDTO.Response>> getPriorities() {
    return ResponseEntity.ok(catalogService.getAllPriorities());
  }

  @PostMapping("/priorities")
  public ResponseEntity<QueuePriorityLevelDTO.Response> createPriority(
      @Valid @RequestBody QueuePriorityLevelDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createPriority(req));
  }

  // Statuses
  @GetMapping("/statuses")
  public ResponseEntity<List<ReservationStatusDTO.Response>> getStatuses() {
    return ResponseEntity.ok(catalogService.getAllStatuses());
  }

  @PostMapping("/statuses")
  public ResponseEntity<ReservationStatusDTO.Response> createStatus(
      @Valid @RequestBody ReservationStatusDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createStatus(req));
  }

  // Reception Stations
  @GetMapping("/stations")
  public ResponseEntity<List<ReceptionStationDTO.Response>> getStations() {
    return ResponseEntity.ok(catalogService.getAllStations());
  }

  @PostMapping("/stations")
  public ResponseEntity<ReceptionStationDTO.Response> createStation(
      @Valid @RequestBody ReceptionStationDTO.Request req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createStation(req));
  }
}
