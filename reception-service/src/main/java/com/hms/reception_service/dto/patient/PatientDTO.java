package com.hms.reception_service.dto.patient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

public class PatientDTO {

  public record CreateRequest(
      String nationalId,
      @NotBlank(message = "First name is required") String firstName,
      @NotBlank(message = "Last name is required") String lastName,
      @NotNull(message = "Date of birth is required") LocalDate dateOfBirth,
      @NotBlank(message = "Gender is required") String gender,
      String bloodGroup,
      @NotBlank(message = "Phone number is required") String phone,
      String emergencyContactPhone,
      String emergencyContactName,
      String allergies,
      String chronicConditions) {}

  public record UpdateRequest(
      String nationalId,
      String firstName,
      String lastName,
      LocalDate dateOfBirth,
      String gender,
      String bloodGroup,
      String phone,
      String emergencyContactPhone,
      String emergencyContactName,
      String allergies,
      String chronicConditions,
      Boolean isActive) {}

  @Schema(name = "PatientResponse")
  public record Response(
      UUID id,
      String mrn,
      String nationalId,
      String firstName,
      String lastName,
      LocalDate dateOfBirth,
      String gender,
      String bloodGroup,
      String phone,
      String emergencyContactPhone,
      String emergencyContactName,
      String allergies,
      String chronicConditions,
      Boolean isActive,
      Date dateCreated,
      Date dateModified) {}
}
