package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.UpdateMedicalOrganizationRequestDTO;
import com.hms.hr_service.model.MedicalOrganization;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MedicalOrganizationMapper {

  MedicalOrganizationResponseDTO medicalOrganizationToMedicalOrganizationResponseDTO(
      MedicalOrganization medicalOrganization);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "adminId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  MedicalOrganization createMedicalOrganizationRequestDTOToMedicalOrganization(
      CreateMedicalOrganizationRequestDTO medicalOrganizationRequestDTO);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "adminId", source = "adminId")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  MedicalOrganization createMedicalOrganizationRequestDTOToMedicalOrganization(
      CreateMedicalOrganizationRequestDTO medicalOrganizationRequestDTO, java.util.UUID adminId);

  CreateMedicalOrganizationResponseDTO medicalOrganizationToCreateMedicalOrganizationResponseDTO(
      MedicalOrganization save);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "adminId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateMedicalOrganizationFromDTO(
      UpdateMedicalOrganizationRequestDTO dto, @MappingTarget MedicalOrganization mo);
}
