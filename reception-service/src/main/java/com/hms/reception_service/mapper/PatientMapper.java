package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.model.Patient;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PatientMapper {

  PatientDTO.Response toResponse(Patient patient);

  List<PatientDTO.Response> toResponseList(List<Patient> patients);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "mrn", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  @Mapping(
      target = "nationalId",
      expression =
          "java(req.nationalId() != null && !req.nationalId().isBlank() ? req.nationalId() : null)")
  @Mapping(target = "bloodGroup", defaultExpression = "java(\"UNKNOWN\")")
  Patient toEntity(PatientDTO.CreateRequest req);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "mrn", ignore = true)
  @Mapping(target = "nationalId", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  void updatePatientFromDTO(PatientDTO.UpdateRequest req, @MappingTarget Patient patient);
}
