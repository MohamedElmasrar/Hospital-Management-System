package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.order.BillableOrderResponseDTO;
import com.hms.billing_service.dto.order.CreateBillableOrderRequestDTO;
import com.hms.billing_service.exception.BillableOrderNotFoundException;
import com.hms.billing_service.exception.InvalidOperationException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.exception.TariffVersionNotFoundException;
import com.hms.billing_service.grpc.HrGrpcClient;
import com.hms.billing_service.mapper.BillableOrderMapper;
import com.hms.billing_service.messaging.AnalyticsEventPublisher;
import com.hms.billing_service.model.BillableOrder;
import com.hms.billing_service.model.DiagnosticOrderDetail;
import com.hms.billing_service.model.ServiceTariffVersion;
import com.hms.billing_service.repository.BillableOrderRepository;
import com.hms.billing_service.repository.ServiceTariffVersionRepository;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillableOrderService {

  private final BillableOrderRepository billableOrderRepository;
  private final ServiceTariffVersionRepository tariffVersionRepository;
  private final BillableOrderMapper billableOrderMapper;
  private final AnalyticsEventPublisher analyticsEventPublisher;
  private final HrGrpcClient hrGrpcClient;

  @Transactional
  public BillableOrderResponseDTO create(CreateBillableOrderRequestDTO request) {
    if (billableOrderRepository.existsByExternalOrderIdAndOrderType(
        request.externalOrderId(), request.orderType())) {
      throw new ResourceAlreadyExistsException(
          "Billable order already exists for externalOrderId "
              + request.externalOrderId()
              + " with orderType "
              + request.orderType());
    }

    if (request.practitionerId() != null
        && !hrGrpcClient.validatePhysician(request.practitionerId())) {
      throw new InvalidOperationException(
          "Practitioner/Physician does not exist with ID: " + request.practitionerId());
    }

    ServiceTariffVersion tariffVersion;
    if (request.tariffVersionId() != null) {
      tariffVersion =
          tariffVersionRepository
              .findById(request.tariffVersionId())
              .orElseThrow(
                  () ->
                      new TariffVersionNotFoundException(
                          "Tariff version not found with ID: " + request.tariffVersionId()));
    } else if (request.serviceCatalogId() != null) {
      Date performedAt = request.performedAt() != null ? request.performedAt() : new Date();
      tariffVersion =
          tariffVersionRepository
              .findActiveTariff(request.serviceCatalogId(), performedAt)
              .orElseThrow(
                  () ->
                      new TariffVersionNotFoundException(
                          "No active tariff version found for catalog item: "
                              + request.serviceCatalogId()));
    } else {
      throw new InvalidOperationException(
          "Either tariffVersionId or serviceCatalogId must be provided");
    }

    BillableOrder order = new BillableOrder();
    order.setExternalOrderId(request.externalOrderId());
    order.setOrderType(request.orderType());
    order.setPatientId(request.patientId());
    order.setPractitionerId(request.practitionerId());
    order.setDepartmentId(request.departmentId());
    order.setTariffVersion(tariffVersion);
    order.setPerformedAt(request.performedAt() != null ? request.performedAt() : new Date());

    if (request.diagnosticDetails() != null) {
      DiagnosticOrderDetail detail =
          billableOrderMapper.toDiagnosticOrderDetail(request.diagnosticDetails());
      order.setDiagnosticOrderDetail(detail);
    }

    BillableOrder saved = billableOrderRepository.save(order);
    analyticsEventPublisher.publish("BILLABLE_ORDER_CREATED", "BILLABLE_ORDER", saved.getId());
    return billableOrderMapper.billableOrderToBillableOrderResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public BillableOrderResponseDTO getById(UUID id) {
    BillableOrder order =
        billableOrderRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new BillableOrderNotFoundException("Billable order not found with ID: " + id));
    return billableOrderMapper.billableOrderToBillableOrderResponseDTO(order);
  }

  @Transactional(readOnly = true)
  public PageResponse<BillableOrderResponseDTO> getByPatient(UUID patientId, Pageable pageable) {
    return PageResponse.from(
        billableOrderRepository
            .findByPatientId(patientId, pageable)
            .map(billableOrderMapper::billableOrderToBillableOrderResponseDTO));
  }

  @Transactional(readOnly = true)
  public PageResponse<BillableOrderResponseDTO> getByPractitioner(
      UUID practitionerId, Pageable pageable) {
    return PageResponse.from(
        billableOrderRepository
            .findByPractitionerId(practitionerId, pageable)
            .map(billableOrderMapper::billableOrderToBillableOrderResponseDTO));
  }
}
