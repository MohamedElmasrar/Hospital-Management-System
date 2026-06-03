package com.hms.hr_service.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hms.hr_service.dto.payroll.PayrollDTO;
import com.hms.hr_service.enums.*;
import com.hms.hr_service.mapper.PayrollMapper;
import com.hms.hr_service.messaging.*;
import com.hms.hr_service.model.*;
import com.hms.hr_service.repository.*;
import com.hms.hr_service.security.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {
  @Mock PayrollSettingsRepository settings;
  @Mock EmployeeCompensationRepository compensations;
  @Mock PayrollRecordRepository records;
  @Mock EmployeeRepository employees;
  @Mock EmployeeAssignmentRepository assignments;
  @Mock MedicalOrganizationRepository organizations;
  @Mock HrAccessService access;
  @Mock NotificationEventPublisher notifications;
  @Mock AnalyticsEventPublisher analytics;
  PayrollMapper mapper = Mappers.getMapper(PayrollMapper.class);
  PayrollService service;
  UUID org = UUID.randomUUID(), employeeId = UUID.randomUUID(), userId = UUID.randomUUID();
  AuthenticatedPrincipal principal =
      new AuthenticatedPrincipal(UUID.randomUUID(), UserRole.HR_HEAD);
  Employee employee;

  @BeforeEach
  void setup() {
    service =
        new PayrollService(
            settings,
            compensations,
            records,
            employees,
            assignments,
            organizations,
            access,
            mapper,
            notifications,
            analytics);
    employee = new Employee();
    employee.setId(employeeId);
    employee.setUserId(userId);
    employee.setGivenName("Sara");
    employee.setFamilyName("Demo");
  }

  PayrollDTO.RecordRequest request(String overtime, String deductions) {
    return new PayrollDTO.RecordRequest(
        employeeId, "2026-11", new BigDecimal(overtime), new BigDecimal(deductions));
  }

  void setupCalculation() {
    when(organizations.existsById(org)).thenReturn(true);
    when(assignments.exists(ArgumentMatchers.<Specification<EmployeeAssignment>>any()))
        .thenReturn(true);
    when(employees.findById(employeeId)).thenReturn(Optional.of(employee));
    var compensation = new EmployeeCompensation();
    compensation.setEmployee(employee);
    compensation.setOrganizationId(org);
    compensation.setBaseSalary(new BigDecimal("8000.00"));
    compensation.setAllowance(new BigDecimal("500.00"));
    compensation.setDeductions(new BigDecimal("200.00"));
    when(compensations.findByOrganizationIdAndEmployeeId(org, employeeId))
        .thenReturn(Optional.of(compensation));
    var config = new PayrollSettings();
    config.setOrganizationId(org);
    config.setOvertimeHourlyRate(new BigDecimal("37.25"));
    when(settings.findById(org)).thenReturn(Optional.of(config));
  }

  @Test
  void calculatesServerSideAndMapsEmployeeAndSnapshotAmounts() {
    setupCalculation();
    when(records.saveAndFlush(any()))
        .thenAnswer(
            a -> {
              PayrollRecord r = a.getArgument(0);
              r.setId(UUID.randomUUID());
              return r;
            });
    var result = service.create(org, request("1.50", "50.00"), principal);
    assertThat(result.employeeName()).isEqualTo("Sara Demo");
    assertThat(result.employeeId()).isEqualTo(employeeId);
    assertThat(result.overtimePay()).isEqualByComparingTo("55.88");
    assertThat(result.grossPay()).isEqualByComparingTo("8555.88");
    assertThat(result.netPay()).isEqualByComparingTo("8305.88");
    assertThat(result.deductions()).isEqualByComparingTo("250.00");
    assertThat(result.status()).isEqualTo(PayrollStatus.DRAFT);
    assertThat(result.payDate()).isEqualTo(LocalDate.of(2026, 11, 28));
  }

  @Test
  void refusesDeductionsGreaterThanGross() {
    setupCalculation();
    assertThatThrownBy(() -> service.create(org, request("0", "10000"), principal))
        .isInstanceOf(IllegalArgumentException.class);
    verify(records, never()).saveAndFlush(any());
  }

  @Test
  void duplicateMonthIsRejectedBeforeCreatingAnotherPayroll() {
    when(organizations.existsById(org)).thenReturn(true);
    when(assignments.exists(ArgumentMatchers.<Specification<EmployeeAssignment>>any()))
        .thenReturn(true);
    when(employees.findById(employeeId)).thenReturn(Optional.of(employee));
    when(records.existsByOrganizationIdAndEmployeeIdAndPeriod(
            org, employeeId, LocalDate.of(2026, 11, 1)))
        .thenReturn(true);
    assertThatThrownBy(() -> service.create(org, request("0", "0"), principal))
        .isInstanceOf(ResponseStatusException.class);
    verify(records, never()).saveAndFlush(any());
  }

  PayrollRecord record(PayrollStatus status) {
    var record = new PayrollRecord();
    record.setId(UUID.randomUUID());
    record.setOrganizationId(org);
    record.setEmployee(employee);
    record.setStatus(status);
    record.setPeriod(LocalDate.of(2026, 11, 1));
    record.setNetPay(new BigDecimal("8300.00"));
    record.setCurrency("MAD");
    when(records.findOneById(record.getId())).thenReturn(Optional.of(record));
    when(organizations.existsById(org)).thenReturn(true);
    return record;
  }

  @Test
  void paymentRequiresApprovalAndIsIdempotent() {
    var record = record(PayrollStatus.DRAFT);
    assertThatThrownBy(() -> service.markPaid(record.getId(), principal))
        .isInstanceOf(ResponseStatusException.class);
    service.approve(record.getId(), principal);
    service.markPaid(record.getId(), principal);
    service.markPaid(record.getId(), principal);
    assertThat(record.getStatus()).isEqualTo(PayrollStatus.PAID);
    verify(analytics)
        .publish("PAYROLL_PAID", "PAYROLL", record.getId(), record.getNetPay(), "currency", "MAD");
    verify(notifications).publish(eq(userId), eq("Salary payment recorded"), anyString());
  }

  @Test
  void approvedPayrollCannotBeRecalculated() {
    var record = record(PayrollStatus.APPROVED);
    assertThatThrownBy(() -> service.recalculate(record.getId(), request("10", "0"), principal))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(compensations);
  }

  @Test
  void foreignOrganizationIsDenied() {
    doThrow(new AccessDeniedException("Outside scope"))
        .when(access)
        .requireOrganization(principal, org);
    assertThatThrownBy(() -> service.settings(org, principal))
        .isInstanceOf(AccessDeniedException.class);
    verifyNoInteractions(settings);
  }

  @Test
  void employeeCannotBeConfiguredInAnUnrelatedOrganization() {
    when(organizations.existsById(org)).thenReturn(true);
    when(assignments.exists(ArgumentMatchers.<Specification<EmployeeAssignment>>any()))
        .thenReturn(false);
    assertThatThrownBy(() -> service.compensation(org, employeeId, principal))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(compensations);
  }

  @Test
  void mapperUpdatesDraftInputsWithoutReplacingIdentityStatusOrAudit() {
    var original = new PayrollRecord();
    var id = UUID.randomUUID();
    original.setId(id);
    original.setStatus(PayrollStatus.APPROVED);
    original.setApprovedAt(Instant.parse("2026-11-01T10:00:00Z"));
    original.setPeriod(LocalDate.of(2026, 11, 1));
    original.setVersion(3L);
    var comp = new EmployeeCompensation();
    comp.setEmployee(employee);
    comp.setOrganizationId(org);
    comp.setBaseSalary(BigDecimal.TEN);
    comp.setDeductions(BigDecimal.ZERO);
    var config = new PayrollSettings();
    config.setOrganizationId(org);
    mapper.updateRecord(comp, config, request("2", "0"), original);
    assertThat(original.getId()).isEqualTo(id);
    assertThat(original.getStatus()).isEqualTo(PayrollStatus.APPROVED);
    assertThat(original.getVersion()).isEqualTo(3L);
    assertThat(original.getApprovedAt()).isEqualTo(Instant.parse("2026-11-01T10:00:00Z"));
    assertThat(original.getOvertimeHours()).isEqualByComparingTo("2");
  }
}
