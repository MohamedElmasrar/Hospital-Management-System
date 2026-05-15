package com.hms.billing_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.model.CashDesk;
import com.hms.billing_service.model.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class JpaAuditingTest {

  @Autowired private DepartmentRepository departmentRepository;
  @Autowired private CashDeskRepository cashDeskRepository;

  @Test
  void department_auditingFieldsAutomaticallyPopulated() {
    Department department = new Department();
    department.setName("Radiology Department");
    department.setSection(BillingSectionType.MEDICAL_SERVICE);

    Department saved = departmentRepository.saveAndFlush(department);

    assertThat(saved.getDateCreated()).isNotNull();
    assertThat(saved.getDateModified()).isNotNull();

    var initialCreated = saved.getDateCreated();
    var initialModified = saved.getDateModified();

    saved.setName("Radiology & Imaging");
    Department updated = departmentRepository.saveAndFlush(saved);

    assertThat(updated.getDateCreated()).isEqualTo(initialCreated);
    assertThat(updated.getDateModified()).isAfterOrEqualTo(initialModified);
  }

  @Test
  void cashDesk_auditingFieldsAutomaticallyPopulated() {
    Department department = new Department();
    department.setName("General Reception");
    department.setSection(BillingSectionType.RECEPTION);
    Department savedDept = departmentRepository.saveAndFlush(department);

    CashDesk desk = new CashDesk();
    desk.setDeskCode("DESK-TEST-01");
    desk.setDepartment(savedDept);
    desk.setIsActive(true);

    CashDesk savedDesk = cashDeskRepository.saveAndFlush(desk);

    assertThat(savedDesk.getDateCreated()).isNotNull();
    assertThat(savedDesk.getDateModified()).isNotNull();
  }
}

