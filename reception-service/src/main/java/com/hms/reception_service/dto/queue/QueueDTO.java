package com.hms.reception_service.dto.queue;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

public class QueueDTO {

  public record DailyQueueResponse(
      UUID id,
      UUID departmentId,
      UUID roomId,
      String roomName,
      LocalDate queueDate,
      String currentTicketNumber,
      Integer lastIssuedSequence,
      Boolean isOpen) {}

  public record IssueTicketRequest(
      @NotNull(message = "Reservation ID is required") UUID reservationId, UUID priorityId) {}

  public record TicketResponse(
      UUID id,
      UUID queueId,
      UUID reservationId,
      String ticketCode,
      Integer sequenceNumber,
      UUID priorityId,
      String priorityName,
      Integer priorityWeight,
      String status,
      Date issuedAt,
      Date calledAt,
      Date serviceStartedAt,
      Date completedAt,
      String patientName,
      String roomName) {}
}
