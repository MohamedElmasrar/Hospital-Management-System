package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.cashdesk.CashDeskResponseDTO;
import com.hms.billing_service.dto.cashdesk.CreateCashDeskRequestDTO;
import com.hms.billing_service.dto.cashdesk.UpdateCashDeskRequestDTO;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.Department;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CashDeskMapper {

  @Mapping(source = "department.id", target = "departmentId")
  @Mapping(source = "department.name", target = "departmentName")
  CashDeskResponseDTO cashDeskToCashDeskResponseDTO(CashDesk cashDesk);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deskCode", source = "dto.deskCode")
  @Mapping(target = "department", source = "department")
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  CashDesk createCashDeskRequestDTOToCashDesk(CreateCashDeskRequestDTO dto, Department department);

  List<CashDeskResponseDTO> toCashDeskResponseDTOList(List<CashDesk> cashDesks);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "department", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  void updateCashDeskFromDTO(UpdateCashDeskRequestDTO dto, @MappingTarget CashDesk cashDesk);
}
