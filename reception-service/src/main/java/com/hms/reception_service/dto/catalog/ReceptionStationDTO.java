package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ReceptionStationDTO {

  public record Request(
      @NotBlank(message = "Station code is required") String stationCode,
      @NotBlank(message = "Name is required") String name,
      @NotNull(message = "Department ID is required") UUID departmentId,
      @NotNull(message = "Category ID is required") UUID categoryId,
      Boolean isActive) {}

  public record Response(
      UUID id,
      String stationCode,
      String name,
      UUID departmentId,
      UUID categoryId,
      String categoryName,
      Boolean isActive) {}
}
