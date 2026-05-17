package com.hms.reception_service.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.UUID;

public class ReservationCategoryDTO {

  public record Request(
      @NotBlank(message = "Code is required") String code,
      @NotBlank(message = "Name is required") String name,
      String description,
      Boolean requiresPhysician,
      Boolean requiresPrescription,
      Boolean isActive) {}

  public record Response(
      UUID id,
      String code,
      String name,
      String description,
      Boolean requiresPhysician,
      Boolean requiresPrescription,
      Boolean isActive,
      Date dateCreated) {}
}
