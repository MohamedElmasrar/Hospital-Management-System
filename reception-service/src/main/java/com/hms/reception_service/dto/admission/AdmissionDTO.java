package com.hms.reception_service.dto.admission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

public class AdmissionDTO {
  public record CreateBedRequest(@NotBlank String bedCode, @NotBlank String ward) {}

  public record AdmitRequest(
      @NotNull UUID patientId, @NotNull UUID bedId, @NotBlank String reason) {}

  @Schema(name = "AdmissionResponse")
  @Builder
  public record Response(
      UUID id,
      UUID patientId,
      String patientName,
      String patientPhone,
      UUID bedId,
      String bedCode,
      String ward,
      String reason,
      LocalDateTime admittedAt,
      LocalDateTime dischargedAt) {}
}
