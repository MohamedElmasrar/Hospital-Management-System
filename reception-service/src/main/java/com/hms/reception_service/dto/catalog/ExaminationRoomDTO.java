package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

public class ExaminationRoomDTO {

  public record Request(
      @NotBlank(message = "Room code is required") String roomCode,
      @NotBlank(message = "Name is required") String name,
      @NotNull(message = "Department ID is required") UUID departmentId,
      UUID modalityId,
      Boolean isActive) {}

  public record Response(
      UUID id,
      String roomCode,
      String name,
      UUID departmentId,
      UUID modalityId,
      String modalityName,
      Boolean isActive,
      Date dateCreated) {}
}
