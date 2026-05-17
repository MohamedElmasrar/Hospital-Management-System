package com.hms.reception_service.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

public class DiagnosticPrescriptionDTO {

  public record CreateRequest(
      @NotNull(message = "Patient ID is required") UUID patientId,
      UUID originatingReservationId,
      @NotNull(message = "Modality ID is required") UUID modalityId,
      @NotBlank(message = "Body part is required") String bodyPart,
      Boolean contrastRequired,
      String clinicalNotes) {}

  public record Response(
      UUID id,
      String prescriptionCode,
      UUID patientId,
      String patientName,
      UUID referringPractitionerId,
      UUID originatingReservationId,
      UUID modalityId,
      String modalityName,
      String modalityCode,
      String bodyPart,
      Boolean contrastRequired,
      String clinicalNotes,
      Boolean isFulfilled,
      Date createdAt) {}
}
