package com.hms.reception_service.controller;

import com.hms.reception_service.dto.catalog.ExaminationRoomDTO;
import com.hms.reception_service.service.AdminCatalogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reception/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final AdminCatalogService catalogService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR', 'RECEPTIONIST')")
  public List<ExaminationRoomDTO.Response> listRooms() {
    return catalogService.getAllRooms();
  }
}
