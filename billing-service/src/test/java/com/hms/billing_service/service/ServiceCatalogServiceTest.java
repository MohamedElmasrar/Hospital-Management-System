package com.hms.billing_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hms.billing_service.dto.catalog.CreateServiceCatalogRequestDTO;
import com.hms.billing_service.dto.catalog.CreateTariffVersionRequestDTO;
import com.hms.billing_service.dto.catalog.ServiceCatalogResponseDTO;
import com.hms.billing_service.dto.catalog.TariffVersionResponseDTO;
import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.exception.ResourceAlreadyExistsException;
import com.hms.billing_service.mapper.ServiceCatalogMapper;
import com.hms.billing_service.model.Department;
import com.hms.billing_service.model.ServiceCatalog;
import com.hms.billing_service.model.ServiceTariffVersion;
import com.hms.billing_service.repository.DepartmentRepository;
import com.hms.billing_service.repository.ServiceCatalogRepository;
import com.hms.billing_service.repository.ServiceTariffVersionRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

  @Mock private ServiceCatalogRepository serviceCatalogRepository;
  @Mock private ServiceTariffVersionRepository tariffVersionRepository;
  @Mock private DepartmentRepository departmentRepository;

  private ServiceCatalogMapper serviceCatalogMapper;
  private ServiceCatalogService serviceCatalogService;

  @BeforeEach
  void setUp() {
    serviceCatalogMapper = Mappers.getMapper(ServiceCatalogMapper.class);
    serviceCatalogService =
        new ServiceCatalogService(
            serviceCatalogRepository,
            tariffVersionRepository,
            departmentRepository,
            serviceCatalogMapper);
  }

  @Test
  void create_serviceAlreadyExists_throwsException() {
    CreateServiceCatalogRequestDTO request =
        new CreateServiceCatalogRequestDTO(
            "CONS-CARD-01",
            UUID.randomUUID(),
            "Cardiology Consultation",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("0.0500"));

    when(serviceCatalogRepository.existsByServiceCode("CONS-CARD-01")).thenReturn(true);

    assertThatThrownBy(() -> serviceCatalogService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("CONS-CARD-01");
  }

  @Test
  void create_success_createsCatalogAndInitialTariff() {
    UUID deptId = UUID.randomUUID();
    Department dept =
        new Department(
            deptId, "Cardiology", BillingSectionType.MEDICAL_SERVICE, new Date(), new Date());

    CreateServiceCatalogRequestDTO request =
        new CreateServiceCatalogRequestDTO(
            "CONS-CARD-01",
            deptId,
            "Cardiology Consultation",
            null,
            new BigDecimal("100.00"),
            new BigDecimal("0.0500"));

    when(serviceCatalogRepository.existsByServiceCode("CONS-CARD-01")).thenReturn(false);
    when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));

    ServiceCatalog savedCatalog = new ServiceCatalog();
    savedCatalog.setId(UUID.randomUUID());
    savedCatalog.setServiceCode("CONS-CARD-01");
    savedCatalog.setName("Cardiology Consultation");
    savedCatalog.setDepartment(dept);
    savedCatalog.setIsActive(true);

    when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenReturn(savedCatalog);

    ServiceTariffVersion savedTariff = new ServiceTariffVersion();
    savedTariff.setId(UUID.randomUUID());
    savedTariff.setCatalogItem(savedCatalog);
    savedTariff.setUnitPrice(new BigDecimal("100.00"));
    savedTariff.setTaxRate(new BigDecimal("0.0500"));
    savedTariff.setEffectiveFrom(new Date());

    when(tariffVersionRepository.save(any(ServiceTariffVersion.class))).thenReturn(savedTariff);

    ServiceCatalogResponseDTO response = serviceCatalogService.create(request);

    assertThat(response).isNotNull();
    assertThat(response.serviceCode()).isEqualTo("CONS-CARD-01");
    assertThat(response.currentTariff()).isNotNull();
    assertThat(response.currentTariff().unitPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
    verify(tariffVersionRepository).save(any(ServiceTariffVersion.class));
  }

  @Test
  void addTariffVersion_success_closesOldTariffAndSavesNew() {
    UUID serviceId = UUID.randomUUID();
    ServiceCatalog catalog = new ServiceCatalog();
    catalog.setId(serviceId);
    catalog.setServiceCode("CONS-CARD-01");

    ServiceTariffVersion oldTariff = new ServiceTariffVersion();
    oldTariff.setId(UUID.randomUUID());
    oldTariff.setCatalogItem(catalog);
    oldTariff.setUnitPrice(new BigDecimal("100.00"));
    oldTariff.setEffectiveFrom(new Date(System.currentTimeMillis() - 100000));
    oldTariff.setEffectiveTo(null);

    when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(catalog));
    when(tariffVersionRepository
            .findFirstByCatalogItemIdAndEffectiveToIsNullOrderByEffectiveFromDesc(serviceId))
        .thenReturn(Optional.of(oldTariff));

    Date newEffectiveFrom = new Date();
    CreateTariffVersionRequestDTO request =
        new CreateTariffVersionRequestDTO(
            new BigDecimal("120.00"), new BigDecimal("0.0500"), newEffectiveFrom);

    ServiceTariffVersion newTariff = new ServiceTariffVersion();
    newTariff.setId(UUID.randomUUID());
    newTariff.setCatalogItem(catalog);
    newTariff.setUnitPrice(new BigDecimal("120.00"));
    newTariff.setEffectiveFrom(newEffectiveFrom);

    when(tariffVersionRepository.save(any(ServiceTariffVersion.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    TariffVersionResponseDTO result = serviceCatalogService.addTariffVersion(serviceId, request);

    assertThat(result.unitPrice()).isEqualByComparingTo(new BigDecimal("120.00"));
    assertThat(oldTariff.getEffectiveTo()).isEqualTo(newEffectiveFrom);
    verify(tariffVersionRepository, times(2)).save(any(ServiceTariffVersion.class));
  }
}
