package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.department.CreateDepartmentRequestDTO;
import com.hms.billing_service.dto.department.DepartmentResponseDTO;
import com.hms.billing_service.dto.department.UpdateDepartmentRequestDTO;
import com.hms.billing_service.model.Department;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {

  DepartmentResponseDTO departmentToDepartmentResponseDTO(Department department);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  Department createDepartmentRequestDTOToDepartment(CreateDepartmentRequestDTO dto);

  List<DepartmentResponseDTO> toDepartmentResponseDTOList(List<Department> departments);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  void updateDepartmentFromDTO(
      UpdateDepartmentRequestDTO dto, @MappingTarget Department department);
}

