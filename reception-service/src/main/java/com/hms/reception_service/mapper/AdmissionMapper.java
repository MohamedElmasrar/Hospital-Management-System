package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.admission.AdmissionDTO;
import com.hms.reception_service.dto.admission.EmergencyAdmissionDTO;
import com.hms.reception_service.model.Admission;
import com.hms.reception_service.model.Bed;
import com.hms.reception_service.model.EmergencyAdmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdmissionMapper {
  @Mapping(source = "patient.id", target = "patientId")
  @Mapping(
      target = "patientName",
      expression =
          "java(admission.getPatient().getFirstName() + \" \" + admission.getPatient().getLastName())")
  @Mapping(source = "patient.phone", target = "patientPhone")
  @Mapping(source = "bed.id", target = "bedId")
  @Mapping(source = "bed.bedCode", target = "bedCode")
  @Mapping(source = "bed.ward", target = "ward")
  AdmissionDTO.Response toResponse(Admission admission);

  @Mapping(source = "patientId", target = "patientId")
  @Mapping(source = "id", target = "bedId")
  @Mapping(source = "bedCode", target = "bedCode")
  @Mapping(source = "ward", target = "ward")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "patientName", ignore = true)
  @Mapping(target = "patientPhone", ignore = true)
  @Mapping(target = "reason", ignore = true)
  @Mapping(target = "admittedAt", ignore = true)
  @Mapping(target = "dischargedAt", ignore = true)
  AdmissionDTO.Response toBedResponse(Bed bed);

  @Mapping(source = "patient.id", target = "patientId")
  EmergencyAdmissionDTO.Response toEmergencyResponse(EmergencyAdmission admission);
}
