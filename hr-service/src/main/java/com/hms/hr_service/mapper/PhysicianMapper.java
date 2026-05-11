package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.Physician.CreatePhysicianRequestDTO;
import com.hms.hr_service.dto.Physician.PhysicianResponseDTO;
import com.hms.hr_service.dto.Physician.PhysicianSpecialtyResponseDTO;
import com.hms.hr_service.dto.Physician.UpdatePhysicianRequestDTO;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.MedicalSpecialty;
import com.hms.hr_service.model.Physician;
import com.hms.hr_service.model.PhysicianSpecialty;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhysicianMapper {

  @Mapping(source = "physician.id", target = "id")
  @Mapping(source = "physician.employee.id", target = "employeeId")
  @Mapping(source = "physician.employee.identifier", target = "employeeIdentifier")
  @Mapping(
      target = "employeeName",
      expression =
          "java(physician.getEmployee() != null ? physician.getEmployee().getGivenName() + \" \" + physician.getEmployee().getFamilyName() : null)")
  @Mapping(source = "physician.licenseNumber", target = "licenseNumber")
  @Mapping(source = "specialties", target = "specialties")
  PhysicianResponseDTO toPhysicianResponseDTO(
      Physician physician, List<PhysicianSpecialtyResponseDTO> specialties);

  @Mapping(source = "medicalSpecialty.id", target = "specialtyId")
  @Mapping(source = "medicalSpecialty.name", target = "specialtyName")
  @Mapping(source = "medicalSpecialty.description", target = "description")
  @Mapping(source = "primary", target = "isPrimary")
  PhysicianSpecialtyResponseDTO toPhysicianSpecialtyResponseDTO(PhysicianSpecialty specialty);

  List<PhysicianSpecialtyResponseDTO> toPhysicianSpecialtyResponseDTOList(
      List<PhysicianSpecialty> specialties);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", source = "employee")
  @Mapping(target = "licenseNumber", source = "dto.licenseNumber")
  Physician toPhysician(CreatePhysicianRequestDTO dto, Employee employee);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  @Mapping(target = "licenseNumber", source = "dto.licenseNumber")
  void updatePhysicianFromDTO(UpdatePhysicianRequestDTO dto, @MappingTarget Physician physician);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "physician", source = "physician")
  @Mapping(target = "medicalSpecialty", source = "medicalSpecialty")
  @Mapping(target = "primary", source = "isPrimary")
  PhysicianSpecialty toPhysicianSpecialty(
      Physician physician, MedicalSpecialty medicalSpecialty, boolean isPrimary);
}
