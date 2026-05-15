package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.catalog.CreateServiceCatalogRequestDTO;
import com.hms.billing_service.dto.catalog.CreateTariffVersionRequestDTO;
import com.hms.billing_service.dto.catalog.ServiceCatalogResponseDTO;
import com.hms.billing_service.dto.catalog.TariffVersionResponseDTO;
import com.hms.billing_service.dto.catalog.UpdateServiceCatalogRequestDTO;
import com.hms.billing_service.exception.DepartmentNotFoundException;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.exception.ServiceCatalogNotFoundException;
import com.hms.billing_service.mapper.ServiceCatalogMapper;
import com.hms.billing_service.model.Department;
import com.hms.billing_service.model.ServiceCatalog;
import com.hms.billing_service.model.ServiceTariffVersion;
import com.hms.billing_service.repository.DepartmentRepository;
import com.hms.billing_service.repository.ServiceCatalogRepository;
import com.hms.billing_service.repository.ServiceTariffVersionRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

  private final ServiceCatalogRepository serviceCatalogRepository;
  private final ServiceTariffVersionRepository tariffVersionRepository;
  private final DepartmentRepository departmentRepository;
  private final ServiceCatalogMapper serviceCatalogMapper;

  @Transactional
  public ServiceCatalogResponseDTO create(CreateServiceCatalogRequestDTO request) {
    if (serviceCatalogRepository.existsByServiceCode(request.serviceCode())) {
      throw new ResourceAlreadyExistsException(
          "Service already exists with code: " + request.serviceCode());
    }

    Department department =
        departmentRepository
            .findById(request.departmentId())
            .orElseThrow(
                () ->
                    new DepartmentNotFoundException(
                        "Department not found with ID: " + request.departmentId()));

    ServiceCatalog catalog =
        serviceCatalogMapper.createServiceCatalogRequestDTOToServiceCatalog(request, department);
    ServiceCatalog savedCatalog = serviceCatalogRepository.save(catalog);

    ServiceTariffVersion initialTariff = new ServiceTariffVersion();
    initialTariff.setCatalogItem(savedCatalog);
    initialTariff.setUnitPrice(request.initialUnitPrice());
    initialTariff.setTaxRate(
        request.initialTaxRate() != null ? request.initialTaxRate() : BigDecimal.ZERO);
    initialTariff.setEffectiveFrom(new Date());
    initialTariff.setEffectiveTo(null);
    ServiceTariffVersion savedTariff = tariffVersionRepository.save(initialTariff);

    TariffVersionResponseDTO tariffDTO =
        serviceCatalogMapper.tariffVersionToTariffVersionResponseDTO(savedTariff);
    return serviceCatalogMapper.serviceCatalogToServiceCatalogResponseDTO(savedCatalog, tariffDTO);
  }

  @Transactional
  public TariffVersionResponseDTO addTariffVersion(
      UUID serviceCatalogId, CreateTariffVersionRequestDTO request) {
    ServiceCatalog catalog =
        serviceCatalogRepository
            .findById(serviceCatalogId)
            .orElseThrow(
                () ->
                    new ServiceCatalogNotFoundException(
                        "Service catalog not found with ID: " + serviceCatalogId));

    Date effectiveFrom = request.effectiveFrom() != null ? request.effectiveFrom() : new Date();

    Optional<ServiceTariffVersion> activeTariffOpt =
        tariffVersionRepository
            .findFirstByCatalogItemIdAndEffectiveToIsNullOrderByEffectiveFromDesc(serviceCatalogId);

    activeTariffOpt.ifPresent(
        current -> {
          current.setEffectiveTo(effectiveFrom);
          tariffVersionRepository.save(current);
        });

    ServiceTariffVersion newTariff = new ServiceTariffVersion();
    newTariff.setCatalogItem(catalog);
    newTariff.setUnitPrice(request.unitPrice());
    newTariff.setTaxRate(request.taxRate() != null ? request.taxRate() : BigDecimal.ZERO);
    newTariff.setEffectiveFrom(effectiveFrom);
    newTariff.setEffectiveTo(null);

    ServiceTariffVersion saved = tariffVersionRepository.save(newTariff);
    return serviceCatalogMapper.tariffVersionToTariffVersionResponseDTO(saved);
  }

  @Transactional
  public ServiceCatalogResponseDTO update(UUID id, UpdateServiceCatalogRequestDTO request) {
    ServiceCatalog catalog =
        serviceCatalogRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ServiceCatalogNotFoundException(
                        "Service catalog not found with ID: " + id));

    if (request.serviceCode() != null
        && !request.serviceCode().equals(catalog.getServiceCode())
        && serviceCatalogRepository.existsByServiceCode(request.serviceCode())) {
      throw new ResourceAlreadyExistsException(
          "Service already exists with code: " + request.serviceCode());
    }

    if (request.departmentId() != null
        && !request.departmentId().equals(catalog.getDepartment().getId())) {
      Department department =
          departmentRepository
              .findById(request.departmentId())
              .orElseThrow(
                  () ->
                      new DepartmentNotFoundException(
                          "Department not found with ID: " + request.departmentId()));
      catalog.setDepartment(department);
    }

    serviceCatalogMapper.updateServiceCatalogFromDTO(request, catalog);
    ServiceCatalog updated = serviceCatalogRepository.save(catalog);
    TariffVersionResponseDTO currentTariff = getActiveTariffDTO(updated.getId());
    return serviceCatalogMapper.serviceCatalogToServiceCatalogResponseDTO(updated, currentTariff);
  }

  @Transactional(readOnly = true)
  public ServiceCatalogResponseDTO getById(UUID id) {
    ServiceCatalog catalog =
        serviceCatalogRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ServiceCatalogNotFoundException(
                        "Service catalog not found with ID: " + id));
    TariffVersionResponseDTO currentTariff = getActiveTariffDTO(catalog.getId());
    return serviceCatalogMapper.serviceCatalogToServiceCatalogResponseDTO(catalog, currentTariff);
  }

  @Transactional(readOnly = true)
  public PageResponse<ServiceCatalogResponseDTO> getAll(Pageable pageable) {
    return PageResponse.from(
        serviceCatalogRepository
            .findAll(pageable)
            .map(
                cat ->
                    serviceCatalogMapper.serviceCatalogToServiceCatalogResponseDTO(
                        cat, getActiveTariffDTO(cat.getId()))));
  }

  @Transactional(readOnly = true)
  public PageResponse<ServiceCatalogResponseDTO> getByDepartment(
      UUID departmentId, Pageable pageable) {
    return PageResponse.from(
        serviceCatalogRepository
            .findByDepartmentId(departmentId, pageable)
            .map(
                cat ->
                    serviceCatalogMapper.serviceCatalogToServiceCatalogResponseDTO(
                        cat, getActiveTariffDTO(cat.getId()))));
  }

  @Transactional(readOnly = true)
  public List<TariffVersionResponseDTO> getTariffHistory(UUID serviceCatalogId) {
    if (!serviceCatalogRepository.existsById(serviceCatalogId)) {
      throw new ServiceCatalogNotFoundException(
          "Service catalog not found with ID: " + serviceCatalogId);
    }
    return serviceCatalogMapper.toTariffVersionResponseDTOList(
        tariffVersionRepository.findByCatalogItemIdOrderByEffectiveFromDesc(serviceCatalogId));
  }

  @Transactional(readOnly = true)
  public TariffVersionResponseDTO getActiveTariffDTO(UUID serviceCatalogId) {
    return tariffVersionRepository
        .findActiveTariff(serviceCatalogId, new Date())
        .map(serviceCatalogMapper::tariffVersionToTariffVersionResponseDTO)
        .orElse(null);
  }

  @Transactional
  public void delete(UUID id) {
    ServiceCatalog catalog =
        serviceCatalogRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ServiceCatalogNotFoundException(
                        "Service catalog not found with ID: " + id));
    catalog.setIsActive(false);
    serviceCatalogRepository.save(catalog);
  }
}
