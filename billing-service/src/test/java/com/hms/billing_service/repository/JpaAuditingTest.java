package com.hms.billing_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.ServiceCatalog;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class JpaAuditingTest {

  @Autowired private CashDeskRepository cashDeskRepository;
  @Autowired private ServiceCatalogRepository serviceCatalogRepository;

  @Test
  void cashDesk_auditingFieldsAutomaticallyPopulated() {
    CashDesk desk = new CashDesk();
    desk.setDeskCode("DESK-TEST-01");
    desk.setDepartmentId(UUID.randomUUID());
    desk.setSection(BillingSectionType.RECEPTION);
    desk.setIsActive(true);

    CashDesk savedDesk = cashDeskRepository.saveAndFlush(desk);

    assertThat(savedDesk.getDateCreated()).isNotNull();
    assertThat(savedDesk.getDateModified()).isNotNull();

    var initialCreated = savedDesk.getDateCreated();
    var initialModified = savedDesk.getDateModified();

    savedDesk.setDeskCode("DESK-TEST-02");
    CashDesk updatedDesk = cashDeskRepository.saveAndFlush(savedDesk);

    assertThat(updatedDesk.getDateCreated()).isEqualTo(initialCreated);
    assertThat(updatedDesk.getDateModified()).isAfterOrEqualTo(initialModified);
  }

  @Test
  void serviceCatalog_auditingFieldsAutomaticallyPopulated() {
    ServiceCatalog catalog = new ServiceCatalog();
    catalog.setServiceCode("CONS-CARD-01");
    catalog.setName("Cardiology Consultation");
    catalog.setDepartmentId(UUID.randomUUID());
    catalog.setIsActive(true);

    ServiceCatalog saved = serviceCatalogRepository.saveAndFlush(catalog);

    assertThat(saved.getDateCreated()).isNotNull();
    assertThat(saved.getDateModified()).isNotNull();
  }
}
