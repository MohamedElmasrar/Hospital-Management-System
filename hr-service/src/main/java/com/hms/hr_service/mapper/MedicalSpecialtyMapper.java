package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.Physician.CreateMedicalSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.MedicalSpecialtyResponseDTO;
import com.hms.hr_service.model.MedicalSpecialty;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MedicalSpecialtyMapper {

  MedicalSpecialtyResponseDTO medicalSpecialtyToMedicalSpecialtyResponseDTO(
      MedicalSpecialty specialty);

  List<MedicalSpecialtyResponseDTO> toMedicalSpecialtyResponseDTOList(
      List<MedicalSpecialty> specialties);

  @Mapping(target = "id", ignore = true)
  MedicalSpecialty createMedicalSpecialtyRequestDTOToMedicalSpecialty(
      CreateMedicalSpecialtyRequestDTO dto);
}
