package com.hms.reception_service.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Builder;

public class ScheduleDTO {

  @Builder
  public record CreateRequest(
      @NotNull(message = "Department ID is required") UUID departmentId,
      UUID practitionerId,
      @NotNull(message = "Room ID is required") UUID roomId,
      @NotNull(message = "Day of week is required") DayOfWeek dayOfWeek,
      @NotNull(message = "Start time is required")
          @Schema(type = "string", format = "time", example = "08:00:00")
          LocalTime startTime,
      @NotNull(message = "End time is required")
          @Schema(type = "string", format = "time", example = "08:30:00")
          LocalTime endTime,
      Integer slotDurationMinutes,
      Boolean isActive) {}

  @Builder
  public record Response(
      UUID id,
      UUID departmentId,
      UUID practitionerId,
      UUID roomId,
      String roomName,
      DayOfWeek dayOfWeek,
      @Schema(type = "string", format = "time", example = "08:00:00") LocalTime startTime,
      @Schema(type = "string", format = "time", example = "08:30:00") LocalTime endTime,
      Integer slotDurationMinutes,
      Boolean isActive) {}

  @Builder
  public record AvailableSlot(
      LocalDate date,
      @Schema(type = "string", format = "time", example = "08:00:00") LocalTime startTime,
      @Schema(type = "string", format = "time", example = "08:30:00") LocalTime endTime,
      UUID roomId,
      String roomName,
      UUID practitionerId,
      boolean isAvailable) {}
}
