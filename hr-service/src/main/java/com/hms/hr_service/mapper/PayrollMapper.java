package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.payroll.PayrollDTO;
import com.hms.hr_service.model.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PayrollMapper {
  PayrollDTO.SettingsResponse settings(PayrollSettings settings);

  @Mapping(target = "organizationId", ignore = true)
  void updateSettings(PayrollDTO.SettingsRequest request, @MappingTarget PayrollSettings settings);

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(
      target = "employeeName",
      expression =
          "java(compensation.getEmployee().getGivenName() + \" \" + compensation.getEmployee().getFamilyName())")
  PayrollDTO.CompensationResponse compensation(EmployeeCompensation compensation);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employee", ignore = true)
  @Mapping(target = "organizationId", ignore = true)
  void updateCompensation(
      PayrollDTO.CompensationRequest request, @MappingTarget EmployeeCompensation compensation);

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(
      target = "employeeName",
      expression =
          "java(record.getEmployee().getGivenName() + \" \" + record.getEmployee().getFamilyName())")
  PayrollDTO.RecordResponse record(PayrollRecord record);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "organizationId", source = "compensation.organizationId")
  @Mapping(target = "employee", source = "compensation.employee")
  @Mapping(target = "baseSalary", source = "compensation.baseSalary")
  @Mapping(target = "allowance", source = "compensation.allowance")
  @Mapping(target = "deductions", source = "compensation.deductions")
  @Mapping(target = "period", ignore = true)
  @Mapping(target = "payDate", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "overtimePay", ignore = true)
  @Mapping(target = "grossPay", ignore = true)
  @Mapping(target = "netPay", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "approvedAt", ignore = true)
  @Mapping(target = "approvedBy", ignore = true)
  @Mapping(target = "paidAt", ignore = true)
  @Mapping(target = "paidBy", ignore = true)
  @Mapping(target = "version", ignore = true)
  PayrollRecord create(
      EmployeeCompensation compensation,
      PayrollSettings settings,
      PayrollDTO.RecordRequest request);

  @InheritConfiguration(name = "create")
  void updateRecord(
      EmployeeCompensation compensation,
      PayrollSettings settings,
      PayrollDTO.RecordRequest request,
      @MappingTarget PayrollRecord record);
}
