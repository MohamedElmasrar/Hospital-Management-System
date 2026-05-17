package com.hms.reception_service.controller;

import com.hms.reception_service.dto.queue.QueueDTO;
import com.hms.reception_service.service.QueueService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/queues")
@RequiredArgsConstructor
public class QueueController {

  private final QueueService queueService;

  @PostMapping("/daily")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public ResponseEntity<QueueDTO.DailyQueueResponse> getOrCreateDailyQueue(
      @RequestParam UUID departmentId, @RequestParam UUID roomId) {
    return ResponseEntity.ok(queueService.getOrCreateDailyQueue(departmentId, roomId));
  }

  @PostMapping("/tickets")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public ResponseEntity<QueueDTO.TicketResponse> issueTicket(
      @Valid @RequestBody QueueDTO.IssueTicketRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(queueService.issueTicket(req));
  }

  @PostMapping("/{queueId}/call-next")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<QueueDTO.TicketResponse> callNextTicket(@PathVariable UUID queueId) {
    return ResponseEntity.ok(queueService.callNextTicket(queueId));
  }

  @PatchMapping("/tickets/{ticketId}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<QueueDTO.TicketResponse> updateTicketStatus(
      @PathVariable UUID ticketId, @RequestParam String status) {
    return ResponseEntity.ok(queueService.updateTicketStatus(ticketId, status));
  }

  @GetMapping("/{queueId}/tickets")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'DOCTOR')")
  public ResponseEntity<List<QueueDTO.TicketResponse>> getQueueTickets(@PathVariable UUID queueId) {
    return ResponseEntity.ok(queueService.getQueueTickets(queueId));
  }
}
