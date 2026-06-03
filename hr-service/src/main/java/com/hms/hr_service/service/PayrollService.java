package com.hms.hr_service.service;

import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.payroll.PayrollDTO;
import com.hms.hr_service.enums.PayrollStatus;
import com.hms.hr_service.mapper.PayrollMapper;
import com.hms.hr_service.messaging.*;
import com.hms.hr_service.model.*;
import com.hms.hr_service.repository.*;
import com.hms.hr_service.security.*;
import com.hms.hr_service.specification.PayrollSpecifications;
import java.math.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {
  private final PayrollSettingsRepository settingsRepository;
  private final EmployeeCompensationRepository compensationRepository;
  private final PayrollRecordRepository recordRepository;
  private final EmployeeRepository employeeRepository;
  private final EmployeeAssignmentRepository assignmentRepository;
  private final MedicalOrganizationRepository organizationRepository;
  private final HrAccessService access;
  private final PayrollMapper mapper;
  private final NotificationEventPublisher notifications;
  private final AnalyticsEventPublisher analytics;

  @Transactional(readOnly = true)
  public PayrollDTO.SettingsResponse settings(
      UUID organizationId, AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    return mapper.settings(settingsEntity(organizationId));
  }

  public PayrollDTO.SettingsResponse saveSettings(
      UUID organizationId, PayrollDTO.SettingsRequest request, AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    try {
      Currency.getInstance(request.currency());
    } catch (IllegalArgumentException error) {
      throw new IllegalArgumentException("Use a valid three-letter currency code");
    }
    var entity = settingsEntity(organizationId);
    mapper.updateSettings(request, entity);
    return mapper.settings(settingsRepository.save(entity));
  }

  @Transactional(readOnly = true)
  public PayrollDTO.CompensationResponse compensation(
      UUID organizationId, UUID employeeId, AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    return mapper.compensation(compensationEntity(organizationId, employeeId));
  }

  public PayrollDTO.CompensationResponse saveCompensation(
      UUID organizationId,
      UUID employeeId,
      PayrollDTO.CompensationRequest request,
      AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    if (request.deductions().compareTo(request.baseSalary().add(request.allowance())) > 0)
      throw new IllegalArgumentException("Deductions cannot exceed base salary and allowance");
    var entity = compensationEntity(organizationId, employeeId);
    mapper.updateCompensation(request, entity);
    return mapper.compensation(compensationRepository.save(entity));
  }

  @Transactional(readOnly = true)
  public PageResponse<PayrollDTO.RecordResponse> list(
      UUID organizationId,
      String month,
      PayrollStatus status,
      Pageable pageable,
      AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    return PageResponse.from(
        recordRepository
            .findAll(
                PayrollSpecifications.records(
                    organizationId, null, month == null ? null : period(month), status, false),
                pageable)
            .map(mapper::record));
  }

  @Transactional(readOnly = true)
  public PageResponse<PayrollDTO.RecordResponse> mine(
      Pageable pageable, AuthenticatedPrincipal principal) {
    return PageResponse.from(
        recordRepository
            .findAll(
                PayrollSpecifications.records(
                    null, access.ownEmployeeId(principal), null, null, true),
                pageable)
            .map(mapper::record));
  }

  public PayrollDTO.RecordResponse create(
      UUID organizationId, PayrollDTO.RecordRequest request, AuthenticatedPrincipal principal) {
    requireOrganization(organizationId, principal);
    employeeInOrganization(request.employeeId(), organizationId);
    LocalDate period = period(request.month());
    if (recordRepository.existsByOrganizationIdAndEmployeeIdAndPeriod(
        organizationId, request.employeeId(), period))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Payroll already exists for this employee and month");
    var compensation = configuredCompensation(organizationId, request.employeeId());
    var settings = settingsEntity(organizationId);
    var record = mapper.create(compensation, settings, request);
    record.setPeriod(period);
    calculate(record, settings.getPayDay());
    return mapper.record(recordRepository.saveAndFlush(record));
  }

  public PayrollDTO.RecordResponse recalculate(
      UUID id, PayrollDTO.RecordRequest request, AuthenticatedPrincipal principal) {
    var record = lockedRecord(id, principal);
    if (record.getStatus() != PayrollStatus.DRAFT)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only draft payroll can be recalculated");
    if (!record.getEmployee().getId().equals(request.employeeId())
        || !record.getPeriod().equals(period(request.month())))
      throw new IllegalArgumentException("Employee and month cannot be changed");
    employeeInOrganization(request.employeeId(), record.getOrganizationId());
    var settings = settingsEntity(record.getOrganizationId());
    mapper.updateRecord(
        configuredCompensation(record.getOrganizationId(), request.employeeId()),
        settings,
        request,
        record);
    calculate(record, settings.getPayDay());
    return mapper.record(record);
  }

  public PayrollDTO.RecordResponse approve(UUID id, AuthenticatedPrincipal principal) {
    var record = lockedRecord(id, principal);
    if (record.getStatus() == PayrollStatus.APPROVED) return mapper.record(record);
    if (record.getStatus() != PayrollStatus.DRAFT)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Only drafts can be approved");
    record.setStatus(PayrollStatus.APPROVED);
    record.setApprovedAt(Instant.now());
    record.setApprovedBy(principal.userId());
    analytics.publish("PAYROLL_APPROVED", "PAYROLL", record.getId());
    notifications.publish(
        record.getEmployee().getUserId(),
        "Payroll ready",
        "Your payroll for " + YearMonth.from(record.getPeriod()) + " is available.");
    return mapper.record(record);
  }

  public PayrollDTO.RecordResponse markPaid(UUID id, AuthenticatedPrincipal principal) {
    var record = lockedRecord(id, principal);
    if (record.getStatus() == PayrollStatus.PAID) return mapper.record(record);
    if (record.getStatus() != PayrollStatus.APPROVED)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Approve payroll before recording payment");
    record.setStatus(PayrollStatus.PAID);
    record.setPaidAt(Instant.now());
    record.setPaidBy(principal.userId());
    analytics.publish(
        "PAYROLL_PAID",
        "PAYROLL",
        record.getId(),
        record.getNetPay(),
        "currency",
        record.getCurrency());
    notifications.publish(
        record.getEmployee().getUserId(),
        "Salary payment recorded",
        "Payment for your " + YearMonth.from(record.getPeriod()) + " payroll has been recorded.");
    return mapper.record(record);
  }

  private PayrollRecord lockedRecord(UUID id, AuthenticatedPrincipal principal) {
    var record =
        recordRepository
            .findOneById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payroll not found"));
    requireOrganization(record.getOrganizationId(), principal);
    return record;
  }

  private void requireOrganization(UUID id, AuthenticatedPrincipal principal) {
    access.requireOrganization(principal, id);
    if (!organizationRepository.existsById(id))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
  }

  private PayrollSettings settingsEntity(UUID id) {
    return settingsRepository
        .findById(id)
        .orElseGet(
            () -> {
              var value = new PayrollSettings();
              value.setOrganizationId(id);
              return value;
            });
  }

  private Employee employeeInOrganization(UUID employeeId, UUID organizationId) {
    if (!assignmentRepository.exists(PayrollSpecifications.assigned(employeeId, organizationId)))
      throw new IllegalArgumentException("Employee is not assigned to this organization");
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
  }

  private EmployeeCompensation compensationEntity(UUID organizationId, UUID employeeId) {
    var employee = employeeInOrganization(employeeId, organizationId);
    return compensationRepository
        .findByOrganizationIdAndEmployeeId(organizationId, employeeId)
        .orElseGet(
            () -> {
              var value = new EmployeeCompensation();
              value.setOrganizationId(organizationId);
              value.setEmployee(employee);
              return value;
            });
  }

  private EmployeeCompensation configuredCompensation(UUID organizationId, UUID employeeId) {
    return compensationRepository
        .findByOrganizationIdAndEmployeeId(organizationId, employeeId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Configure employee salary in payroll settings first"));
  }

  private LocalDate period(String month) {
    try {
      var value = YearMonth.parse(month);
      if (value.getYear() < 1900 || value.getYear() > 9999) throw new IllegalArgumentException();
      return value.atDay(1);
    } catch (RuntimeException error) {
      throw new IllegalArgumentException("Use a valid payroll month (YYYY-MM)");
    }
  }

  private void calculate(PayrollRecord record, int payDay) {
    record.setPayDate(record.getPeriod().withDayOfMonth(payDay));
    record.setOvertimePay(
        record
            .getOvertimeHours()
            .multiply(record.getOvertimeHourlyRate())
            .setScale(2, RoundingMode.HALF_UP));
    record.setGrossPay(
        record.getBaseSalary().add(record.getAllowance()).add(record.getOvertimePay()));
    record.setDeductions(record.getDeductions().add(record.getExtraDeductions()));
    record.setNetPay(record.getGrossPay().subtract(record.getDeductions()));
    if (record.getNetPay().signum() < 0)
      throw new IllegalArgumentException("Deductions cannot exceed gross pay");
    if (record.getGrossPay().compareTo(new BigDecimal("9999999999.99")) > 0)
      throw new IllegalArgumentException("Gross pay exceeds the supported amount");
  }
}
