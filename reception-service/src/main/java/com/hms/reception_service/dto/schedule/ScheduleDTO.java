package com.hms.reception_service.dto.schedule;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ScheduleDTO {

  public record CreateRequest(
      @NotNull(message = "Department ID is required") UUID departmentId,
      UUID practitionerId,
      @NotNull(message = "Room ID is required") UUID roomId,
      @NotNull(message = "Day of week is required") DayOfWeek dayOfWeek,
      @NotNull(message = "Start time is required") LocalTime startTime,
      @NotNull(message = "End time is required") LocalTime endTime,
      Integer slotDurationMinutes,
      Boolean isActive) {}

  public record Response(
      UUID id,
      UUID departmentId,
      UUID practitionerId,
      UUID roomId,
      String roomName,
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      Integer slotDurationMinutes,
      Boolean isActive) {}

  public record AvailableSlot(
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      UUID roomId,
      String roomName,
      UUID practitionerId,
      boolean isAvailable) {}
}
