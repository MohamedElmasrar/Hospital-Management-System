package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.Department.CreateDepartmentRequestDTO;
import com.hms.hr_service.dto.Department.DepartmentResponseDTO;
import com.hms.hr_service.dto.Department.UpdateDepartmentRequestDTO;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.MedicalOrganization;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {

  @Mapping(source = "medicalOrganization.id", target = "medicalOrganizationId")
  @Mapping(source = "medicalOrganization.name", target = "medicalOrganizationName")
  @Mapping(source = "parentDepartment.id", target = "parentDepartmentId")
  @Mapping(source = "parentDepartment.name", target = "parentDepartmentName")
  DepartmentResponseDTO departmentToDepartmentResponseDTO(Department department);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "medicalOrganization", ignore = true)
  @Mapping(target = "parentDepartment", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  Department createDepartmentRequestDTOToDepartment(CreateDepartmentRequestDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "description", source = "dto.description")
  @Mapping(target = "medicalOrganization", source = "organization")
  @Mapping(target = "parentDepartment", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  Department createDepartmentRequestDTOToDepartment(
      CreateDepartmentRequestDTO dto, MedicalOrganization organization);

  List<DepartmentResponseDTO> toDepartmentResponseDTOList(List<Department> departments);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "medicalOrganization", ignore = true)
  @Mapping(target = "parentDepartment", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  void updateDepartmentFromDTO(
      UpdateDepartmentRequestDTO dto, @MappingTarget Department department);
}
