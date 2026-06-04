package com.hms.hr_service.mapper;

import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.model.AttendanceRecord;
import com.hms.hr_service.model.EmployeeCredential;
import com.hms.hr_service.model.EmployeeShift;
import com.hms.hr_service.model.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkforceMapper {
  @Mapping(source = "employee.id", target = "employeeId")
  WorkforceDTO.CredentialResponse credential(EmployeeCredential credential);

  @Mapping(source = "employee.id", target = "employeeId")
  @Mapping(
      target = "employeeName",
      expression =
          "java(shift.getEmployee().getGivenName() + \" \" + shift.getEmployee().getFamilyName())")
  @Mapping(source = "department.id", target = "departmentId")
  @Mapping(source = "department.name", target = "departmentName")
  WorkforceDTO.ShiftResponse shift(EmployeeShift shift);

  @Mapping(source = "employee.id", target = "employeeId")
  @Mapping(
      target = "employeeName",
      expression =
          "java(leave.getEmployee().getGivenName() + \" \" + leave.getEmployee().getFamilyName())")
  WorkforceDTO.LeaveResponse leave(LeaveRequest leave);

  @Mapping(source = "employee.id", target = "employeeId")
  @Mapping(
      target = "employeeName",
      expression =
          "java(attendance.getEmployee().getGivenName() + \" \" + attendance.getEmployee().getFamilyName())")
  WorkforceDTO.AttendanceResponse attendance(AttendanceRecord attendance);

  @Mapping(
      target = "netSalary",
      expression =
          "java(request.baseSalary().add(request.overtime()).subtract(request.deductions()))")
  WorkforceDTO.PayrollResponse payroll(WorkforceDTO.PayrollRequest request);
}
