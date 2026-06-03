package com.hms.hr_service.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hms.hr_service.enums.PayrollStatus;
import com.hms.hr_service.model.*;
import com.hms.hr_service.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.grpc.server.port=0",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
    })
@AutoConfigureMockMvc
@Transactional
class PayrollControllerTest {
  @Autowired MockMvc mvc;
  @Autowired EmployeeRepository employees;
  @Autowired MedicalOrganizationRepository organizations;
  @Autowired DepartmentRepository departments;
  @Autowired EmployeeAssignmentRepository assignments;
  @Autowired PayrollRecordRepository records;
  UUID headUser = UUID.randomUUID(), staffUser = UUID.randomUUID();
  MedicalOrganization own, other;
  Employee staff, foreign;

  @BeforeEach
  void fixtures() {
    own = organization();
    other = organization();
    assign(employee(headUser), own);
    staff = employee(staffUser);
    assign(staff, own);
    foreign = employee(UUID.randomUUID());
    assign(foreign, other);
  }

  MedicalOrganization organization() {
    var org = new MedicalOrganization();
    org.setName("Payroll test");
    org.setIdentifier(UUID.randomUUID().toString());
    org.setAdminId(UUID.randomUUID());
    return organizations.saveAndFlush(org);
  }

  Employee employee(UUID user) {
    var employee = new Employee();
    employee.setUserId(user);
    employee.setIdentifier(UUID.randomUUID().toString());
    employee.setGivenName("Payroll");
    employee.setFamilyName("Employee");
    return employees.saveAndFlush(employee);
  }

  void assign(Employee employee, MedicalOrganization org) {
    var department = new Department();
    department.setName("Test department");
    department.setMedicalOrganization(org);
    department = departments.saveAndFlush(department);
    var assignment = new EmployeeAssignment();
    assignment.setEmployee(employee);
    assignment.setDepartment(department);
    assignment.setJobTitle("Staff");
    assignment.setStartDate(java.sql.Date.valueOf(LocalDate.now().minusYears(1)));
    assignments.saveAndFlush(assignment);
  }

  JwtRequestPostProcessor auth(UUID user, String role) {
    return jwt()
        .jwt(j -> j.subject(user.toString()).claim("role", role))
        .authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  PayrollRecord record(
      Employee employee, MedicalOrganization org, PayrollStatus status, LocalDate period) {
    var value = new PayrollRecord();
    value.setOrganizationId(org.getId());
    value.setEmployee(employee);
    value.setPeriod(period);
    value.setCurrency("MAD");
    value.setPayDate(period.withDayOfMonth(28));
    value.setStatus(status);
    value.setBaseSalary(new BigDecimal("8000.00"));
    value.setAllowance(BigDecimal.ZERO);
    value.setOvertimeHours(BigDecimal.ZERO);
    value.setOvertimeHourlyRate(BigDecimal.ZERO);
    value.setOvertimePay(BigDecimal.ZERO);
    value.setExtraDeductions(BigDecimal.ZERO);
    value.setDeductions(BigDecimal.ZERO);
    value.setGrossPay(new BigDecimal("8000.00"));
    value.setNetPay(new BigDecimal("8000.00"));
    return records.saveAndFlush(value);
  }

  @Test
  void employeeOnlySeesPublishedOwnPayrollAndCannotManageSettings() throws Exception {
    var month = LocalDate.now().withDayOfMonth(1);
    var ownRecord = record(staff, own, PayrollStatus.APPROVED, month);
    record(staff, own, PayrollStatus.DRAFT, month.minusMonths(1));
    record(foreign, other, PayrollStatus.PAID, month);
    var auth = auth(staffUser, "EMPLOYEE");
    mvc.perform(get("/payroll/me").with(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].id").value(ownRecord.getId().toString()));
    mvc.perform(get("/payroll/records").param("organizationId", own.getId().toString()).with(auth))
        .andExpect(status().isForbidden());
    mvc.perform(get("/payroll/settings").param("organizationId", own.getId().toString()).with(auth))
        .andExpect(status().isForbidden());
  }

  @Test
  void headCannotReadForeignOrganizationOrApproveItsPayroll() throws Exception {
    var foreignRecord =
        record(foreign, other, PayrollStatus.DRAFT, LocalDate.now().withDayOfMonth(1));
    var auth = auth(headUser, "HR_HEAD");
    mvc.perform(
            get("/payroll/records").param("organizationId", other.getId().toString()).with(auth))
        .andExpect(status().isForbidden());
    mvc.perform(post("/payroll/records/" + foreignRecord.getId() + "/approve").with(auth))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/payroll/compensations/" + foreign.getId())
                .param("organizationId", own.getId().toString())
                .with(auth))
        .andExpect(status().isBadRequest());
  }

  @Test
  void invalidSettingsAndAnonymousRequestsAreRejected() throws Exception {
    mvc.perform(get("/payroll/me")).andExpect(status().isUnauthorized());
    mvc.perform(
            put("/payroll/settings")
                .param("organizationId", own.getId().toString())
                .with(auth(headUser, "HR_HEAD"))
                .contentType("application/json")
                .content("{\"currency\":\"MAD\",\"payDay\":31,\"overtimeHourlyRate\":-1}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void completeWorkflowKeepsApprovedAmountsAfterSettingsChange() throws Exception {
    var head = auth(headUser, "HR_HEAD");
    String org = own.getId().toString();
    mvc.perform(
            put("/payroll/settings")
                .param("organizationId", org)
                .with(head)
                .contentType("application/json")
                .content(
                    """
        {"currency":"MAD","payDay":28,"overtimeHourlyRate":50}
        """))
        .andExpect(status().isOk());
    mvc.perform(
            put("/payroll/compensations/" + staff.getId())
                .param("organizationId", org)
                .with(head)
                .contentType("application/json")
                .content(
                    """
        {"baseSalary":8000,"allowance":500,"deductions":200}
        """))
        .andExpect(status().isOk());
    String request =
        """
        {"employeeId":"%s","month":"2027-01","overtimeHours":4,"extraDeductions":0}
        """
            .formatted(staff.getId());
    mvc.perform(
            post("/payroll/records")
                .param("organizationId", org)
                .with(head)
                .contentType("application/json")
                .content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.netPay").value(8500));
    String id = records.findAll().getFirst().getId().toString();
    mvc.perform(
            post("/payroll/records")
                .param("organizationId", org)
                .with(head)
                .contentType("application/json")
                .content(request))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/payroll/records/" + id)
                .with(head)
                .contentType("application/json")
                .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.netPay").value(8500));
    mvc.perform(post("/payroll/records/" + id + "/pay").with(head))
        .andExpect(status().isConflict());
    mvc.perform(post("/payroll/records/" + id + "/approve").with(head)).andExpect(status().isOk());
    mvc.perform(
            put("/payroll/settings")
                .param("organizationId", org)
                .with(head)
                .contentType("application/json")
                .content(
                    """
        {"currency":"USD","payDay":15,"overtimeHourlyRate":100}
        """))
        .andExpect(status().isOk());
    mvc.perform(
            put("/payroll/records/" + id)
                .with(head)
                .contentType("application/json")
                .content(request))
        .andExpect(status().isConflict());
    mvc.perform(post("/payroll/records/" + id + "/pay").with(head)).andExpect(status().isOk());
    mvc.perform(post("/payroll/records/" + id + "/pay").with(head)).andExpect(status().isOk());
    mvc.perform(get("/payroll/me").with(auth(staffUser, "EMPLOYEE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].currency").value("MAD"))
        .andExpect(jsonPath("$.content[0].netPay").value(8500));
  }
}
