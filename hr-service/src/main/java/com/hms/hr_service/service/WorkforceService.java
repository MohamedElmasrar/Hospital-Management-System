package com.hms.hr_service.service;

import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.exception.*;
import com.hms.hr_service.model.*;
import com.hms.hr_service.repository.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkforceService {
  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeCredentialRepository credentialRepository;
  private final EmployeeShiftRepository shiftRepository;
  private final LeaveRequestRepository leaveRepository;
  private final AttendanceRecordRepository attendanceRepository;

  @Transactional
  public WorkforceDTO.CredentialResponse addCredential(
      UUID employeeId, WorkforceDTO.CredentialRequest request) {
    Employee employee = employee(employeeId);
    EmployeeCredential credential = new EmployeeCredential();
    credential.setEmployee(employee);
    credential.setCredentialType(request.credentialType());
    credential.setCredentialNumber(request.credentialNumber());
    credential.setExpiresOn(request.expiresOn());
    return credential(credentialRepository.save(credential));
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.CredentialResponse> expiringCredentials(int days) {
    LocalDate limit = LocalDate.now().plusDays(Math.max(days, 0));
    return credentialRepository.findByExpiresOnLessThanEqual(limit).stream()
        .map(this::credential)
        .toList();
  }

  @Transactional
  public WorkforceDTO.ShiftResponse createShift(WorkforceDTO.ShiftRequest request) {
    Employee employee = employee(request.employeeId());
    EmployeeShift shift = new EmployeeShift();
    shift.setEmployee(employee);
    shift.setDepartment(request.departmentId() == null ? null : department(request.departmentId()));
    shift.setShiftDate(request.shiftDate());
    shift.setStartsAt(request.startsAt());
    shift.setEndsAt(request.endsAt());
    return shift(shiftRepository.save(shift));
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.ShiftResponse> shifts(UUID employeeId, LocalDate from, LocalDate to) {
    employee(employeeId);
    return shiftRepository.findByEmployeeIdAndShiftDateBetween(employeeId, from, to).stream()
        .map(this::shift)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.ShiftResponse> availability(LocalDate date) {
    return shiftRepository.findByShiftDate(date).stream().map(this::shift).toList();
  }

  @Transactional
  public WorkforceDTO.LeaveResponse requestLeave(WorkforceDTO.LeaveRequest request) {
    Employee employee = employee(request.employeeId());
    if (request.endsOn().isBefore(request.startsOn()))
      throw new IllegalArgumentException("Leave end precedes start");
    LeaveRequest leave = new LeaveRequest();
    leave.setEmployee(employee);
    leave.setStartsOn(request.startsOn());
    leave.setEndsOn(request.endsOn());
    leave.setReason(request.reason());
    leave.setStatus("PENDING");
    return leave(leaveRepository.save(leave));
  }

  @Transactional
  public WorkforceDTO.LeaveResponse decideLeave(
      UUID id, WorkforceDTO.LeaveDecisionRequest request) {
    LeaveRequest leave =
        leaveRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
    String status = request.status().toUpperCase();
    if (!Set.of("APPROVED", "REJECTED").contains(status))
      throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
    leave.setStatus(status);
    leave.setDecisionReason(request.decisionReason());
    return leave(leaveRepository.save(leave));
  }

  @Transactional
  public WorkforceDTO.AttendanceResponse recordAttendance(WorkforceDTO.AttendanceRequest request) {
    Employee employee = employee(request.employeeId());
    AttendanceRecord record =
        attendanceRepository
            .findByEmployeeIdAndAttendanceDate(request.employeeId(), request.attendanceDate())
            .orElseGet(AttendanceRecord::new);
    record.setEmployee(employee);
    record.setAttendanceDate(request.attendanceDate());
    if (request.checkIn()) record.setCheckedInAt(LocalDateTime.now());
    if (request.checkOut()) record.setCheckedOutAt(LocalDateTime.now());
    return attendance(attendanceRepository.save(record));
  }

  public WorkforceDTO.PayrollResponse payroll(WorkforceDTO.PayrollRequest request) {
    employee(request.employeeId());
    return new WorkforceDTO.PayrollResponse(
        request.employeeId(),
        request.month(),
        request.baseSalary(),
        request.overtime(),
        request.deductions(),
        request.baseSalary().add(request.overtime()).subtract(request.deductions()));
  }

  private Employee employee(UUID id) {
    return employeeRepository.findById(id).orElseThrow(EmployeeNotFoundException::new);
  }

  private Department department(UUID id) {
    return departmentRepository.findById(id).orElseThrow(DepartmentNotFoundException::new);
  }

  private WorkforceDTO.CredentialResponse credential(EmployeeCredential c) {
    return new WorkforceDTO.CredentialResponse(
        c.getId(),
        c.getEmployee().getId(),
        c.getCredentialType(),
        c.getCredentialNumber(),
        c.getExpiresOn());
  }

  private WorkforceDTO.ShiftResponse shift(EmployeeShift s) {
    return new WorkforceDTO.ShiftResponse(
        s.getId(),
        s.getEmployee().getId(),
        s.getDepartment() == null ? null : s.getDepartment().getId(),
        s.getShiftDate(),
        s.getStartsAt(),
        s.getEndsAt());
  }

  private WorkforceDTO.LeaveResponse leave(LeaveRequest l) {
    return new WorkforceDTO.LeaveResponse(
        l.getId(),
        l.getEmployee().getId(),
        l.getStartsOn(),
        l.getEndsOn(),
        l.getReason(),
        l.getStatus(),
        l.getDecisionReason());
  }

  private WorkforceDTO.AttendanceResponse attendance(AttendanceRecord a) {
    return new WorkforceDTO.AttendanceResponse(
        a.getId(),
        a.getEmployee().getId(),
        a.getAttendanceDate(),
        a.getCheckedInAt(),
        a.getCheckedOutAt());
  }
}
