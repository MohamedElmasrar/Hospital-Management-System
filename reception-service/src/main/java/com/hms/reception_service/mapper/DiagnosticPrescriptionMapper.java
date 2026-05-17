package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.prescription.DiagnosticPrescriptionDTO;
import com.hms.reception_service.model.DiagnosticPrescription;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagnosticPrescriptionMapper {

  @Mapping(source = "patient.id", target = "patientId")
  @Mapping(
      target = "patientName",
      expression =
          "java(prescription.getPatient() != null ? prescription.getPatient().getFirstName() + \" \" + prescription.getPatient().getLastName() : null)")
  @Mapping(source = "modality.id", target = "modalityId")
  @Mapping(source = "modality.name", target = "modalityName")
  @Mapping(source = "modality.code", target = "modalityCode")
  DiagnosticPrescriptionDTO.Response toResponse(DiagnosticPrescription prescription);

  List<DiagnosticPrescriptionDTO.Response> toResponseList(
      List<DiagnosticPrescription> prescriptions);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "prescriptionCode", ignore = true)
  @Mapping(target = "patient", ignore = true)
  @Mapping(target = "referringPractitionerId", ignore = true)
  @Mapping(target = "modality", ignore = true)
  @Mapping(
      target = "contrastRequired",
      defaultExpression =
          "java(req.contrastRequired() != null ? req.contrastRequired() : Boolean.FALSE)")
  @Mapping(target = "isFulfilled", constant = "false")
  @Mapping(target = "createdAt", ignore = true)
  DiagnosticPrescription toEntity(DiagnosticPrescriptionDTO.CreateRequest req);
}
