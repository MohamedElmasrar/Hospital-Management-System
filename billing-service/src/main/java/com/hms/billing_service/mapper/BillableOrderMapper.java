package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.catalog.TariffVersionResponseDTO;
import com.hms.billing_service.dto.order.BillableOrderResponseDTO;
import com.hms.billing_service.dto.order.DiagnosticOrderDetailDTO;
import com.hms.billing_service.model.BillableOrder;
import com.hms.billing_service.model.DiagnosticOrderDetail;
import com.hms.billing_service.model.ServiceTariffVersion;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BillableOrderMapper {

  @Mapping(source = "order.tariffVersion.catalogItem.id", target = "serviceCatalogId")
  @Mapping(source = "order.tariffVersion.catalogItem.name", target = "serviceName")
  @Mapping(source = "order.tariffVersion.catalogItem.serviceCode", target = "serviceCode")
  @Mapping(source = "order.tariffVersion", target = "tariffVersion")
  @Mapping(source = "order.diagnosticOrderDetail", target = "diagnosticDetails")
  BillableOrderResponseDTO billableOrderToBillableOrderResponseDTO(BillableOrder order);

  DiagnosticOrderDetailDTO diagnosticOrderDetailToDTO(DiagnosticOrderDetail detail);

  @Mapping(target = "billableOrderId", ignore = true)
  @Mapping(target = "billableOrder", ignore = true)
  @Mapping(target = "contrastUsed", defaultValue = "false")
  DiagnosticOrderDetail toDiagnosticOrderDetail(DiagnosticOrderDetailDTO dto);

  @Mapping(source = "catalogItem.id", target = "catalogItemId")
  TariffVersionResponseDTO toTariffVersionDTO(ServiceTariffVersion tariff);

  List<BillableOrderResponseDTO> toBillableOrderResponseDTOList(List<BillableOrder> orders);
}

