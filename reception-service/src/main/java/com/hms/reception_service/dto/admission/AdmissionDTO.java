package com.hms.reception_service.dto.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdmissionDTO {
  public record CreateBedRequest(@NotBlank String bedCode, @NotBlank String ward) {}

  public record AdmitRequest(
      @NotNull UUID patientId, @NotNull UUID bedId, @NotBlank String reason) {}

  public record Response(
      UUID id,
      UUID patientId,
      UUID bedId,
      String bedCode,
      String ward,
      String reason,
      LocalDateTime admittedAt,
      LocalDateTime dischargedAt) {}
}
