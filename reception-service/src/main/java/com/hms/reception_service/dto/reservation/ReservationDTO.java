package com.hms.reception_service.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public class ReservationDTO {

  public record CreateConsultationRequest(
      @NotNull(message = "Patient ID is required") UUID patientId,
      @NotNull(message = "Department ID is required") UUID departmentId,
      @NotNull(message = "Practitioner ID is required") UUID practitionerId,
      UUID roomId,
      @NotNull(message = "Reservation date is required") LocalDate reservationDate,
      @NotNull(message = "Start time is required") LocalTime startTime,
      @NotNull(message = "End time is required") LocalTime endTime,
      String reasonForVisit,
      String notes) {}

  public record CreateDiagnosticRequest(
      @NotNull(message = "Patient ID is required") UUID patientId,
      @NotNull(message = "Department ID is required") UUID departmentId,
      UUID prescriptionId,
      @NotNull(message = "Modality ID is required") UUID modalityId,
      @NotNull(message = "Room ID is required") UUID roomId,
      @NotNull(message = "Reservation date is required") LocalDate reservationDate,
      @NotNull(message = "Start time is required") LocalTime startTime,
      @NotNull(message = "End time is required") LocalTime endTime,
      String bodyPart,
      Boolean contrastUsed,
      UUID technicianId,
      Boolean safetyChecklistCompleted,
      String reasonForVisit,
      String notes) {}

  public record UpdateStatusRequest(
      @NotNull(message = "Status ID is required") UUID statusId, String notes) {}

  public record DiagnosticDetailResponse(
      UUID modalityId,
      String modalityName,
      String modalityCode,
      String bodyPart,
      Boolean contrastUsed,
      UUID technicianId,
      Boolean safetyChecklistCompleted) {}

  @Schema(name = "ReservationResponse")
  public record Response(
      UUID id,
      String reservationNumber,
      UUID patientId,
      String patientName,
      String patientPhone,
      UUID categoryId,
      String categoryName,
      String categoryCode,
      UUID departmentId,
      UUID practitionerId,
      UUID prescriptionId,
      UUID roomId,
      String roomName,
      LocalDate reservationDate,
      LocalTime startTime,
      LocalTime endTime,
      UUID statusId,
      String statusCode,
      String statusName,
      String badgeColorHex,
      Boolean isPaid,
      UUID billingInvoiceId,
      String reasonForVisit,
      String notes,
      UUID receptionistStaffId,
      DiagnosticDetailResponse diagnosticDetail,
      Date createdAt,
      Date updatedAt) {}
}
