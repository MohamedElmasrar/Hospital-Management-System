package com.hms.hr_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import com.hms.hr_service.model.Department;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.MedicalOrganization;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class JpaAuditingTest {

  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private DepartmentRepository departmentRepository;
  @Autowired private MedicalOrganizationRepository medicalOrganizationRepository;

  @Test
  void employee_auditingFieldsAutomaticallyPopulated() {
    Employee employee = new Employee();
    employee.setIdentifier("AUDIT-EMP-1");
    employee.setGivenName("Alice");
    employee.setFamilyName("Smith");
    employee.setGender(GenderType.Female);
    employee.setEmploymentStatus(EmploymentStatus.Active);

    // Notice we do NOT set dateCreated or dateModified manually
    Employee saved = employeeRepository.saveAndFlush(employee);

    assertThat(saved.getDateCreated()).isNotNull();
    assertThat(saved.getDateModified()).isNotNull();

    var initialCreated = saved.getDateCreated();
    var initialModified = saved.getDateModified();

    saved.setGivenName("Alicia");
    Employee updated = employeeRepository.saveAndFlush(saved);

    assertThat(updated.getDateCreated()).isEqualTo(initialCreated);
    assertThat(updated.getDateModified()).isAfterOrEqualTo(initialModified);
  }

  @Test
  void department_auditingFieldAutomaticallyPopulated() {
    MedicalOrganization org = new MedicalOrganization();
    org.setName("General Hospital");
    org.setIdentifier("AUDIT-ORG-1");
    org.setAdminId(UUID.randomUUID());
    MedicalOrganization savedOrg = medicalOrganizationRepository.saveAndFlush(org);

    Department dept = new Department();
    dept.setName("Cardiology");
    dept.setMedicalOrganization(savedOrg);

    // Notice we do NOT set dateCreated manually
    Department savedDept = departmentRepository.saveAndFlush(dept);

    assertThat(savedDept.getDateCreated()).isNotNull();
  }

  @Test
  void medicalOrganization_auditingFieldsAutomaticallyPopulated() {
    MedicalOrganization org = new MedicalOrganization();
    org.setName("St. Jude Hospital");
    org.setIdentifier("AUDIT-ORG-2");
    org.setAdminId(UUID.randomUUID());

    // Notice we do NOT set createdAt or updatedAt manually
    MedicalOrganization savedOrg = medicalOrganizationRepository.saveAndFlush(org);

    assertThat(savedOrg.getCreatedAt()).isNotNull();
    assertThat(savedOrg.getUpdatedAt()).isNotNull();

    var initialCreated = savedOrg.getCreatedAt();

    savedOrg.setName("St. Jude Medical Center");
    MedicalOrganization updatedOrg = medicalOrganizationRepository.saveAndFlush(savedOrg);

    assertThat(updatedOrg.getCreatedAt()).isEqualTo(initialCreated);
    assertThat(updatedOrg.getUpdatedAt()).isNotNull();
  }
}
