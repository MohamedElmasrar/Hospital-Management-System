package com.hms.reception_service.dto.admission;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class EmergencyAdmissionDTO {
  public record CreateRequest(
      UUID patientId,
      @NotBlank String displayName,
      String phone,
      @NotBlank String chiefComplaint) {}

  public record Response(
      UUID id,
      UUID patientId,
      String displayName,
      String phone,
      String chiefComplaint,
      java.time.LocalDateTime createdAt,
      java.time.LocalDateTime closedAt) {}
}
