package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.catalog.CreateServiceCatalogRequestDTO;
import com.hms.billing_service.dto.catalog.ServiceCatalogResponseDTO;
import com.hms.billing_service.dto.catalog.TariffVersionResponseDTO;
import com.hms.billing_service.dto.catalog.UpdateServiceCatalogRequestDTO;
import com.hms.billing_service.model.Department;
import com.hms.billing_service.model.ServiceCatalog;
import com.hms.billing_service.model.ServiceTariffVersion;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceCatalogMapper {

  @Mapping(source = "catalog.id", target = "id")
  @Mapping(source = "catalog.name", target = "name")
  @Mapping(source = "catalog.department.id", target = "departmentId")
  @Mapping(source = "catalog.department.name", target = "departmentName")
  @Mapping(source = "catalog.dateCreated", target = "dateCreated")
  @Mapping(source = "catalog.dateModified", target = "dateModified")
  @Mapping(source = "currentTariff", target = "currentTariff")
  ServiceCatalogResponseDTO serviceCatalogToServiceCatalogResponseDTO(
      ServiceCatalog catalog, TariffVersionResponseDTO currentTariff);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "dto.name")
  @Mapping(target = "serviceCode", source = "dto.serviceCode")
  @Mapping(target = "externalServiceId", source = "dto.externalServiceId")
  @Mapping(target = "department", source = "department")
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  ServiceCatalog createServiceCatalogRequestDTOToServiceCatalog(
      CreateServiceCatalogRequestDTO dto, Department department);

  @Mapping(source = "catalogItem.id", target = "catalogItemId")
  TariffVersionResponseDTO tariffVersionToTariffVersionResponseDTO(ServiceTariffVersion tariff);

  List<TariffVersionResponseDTO> toTariffVersionResponseDTOList(List<ServiceTariffVersion> tariffs);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "department", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  void updateServiceCatalogFromDTO(
      UpdateServiceCatalogRequestDTO dto, @MappingTarget ServiceCatalog catalog);
}

