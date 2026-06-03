package com.hms.hr_service.service;

import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.exception.*;
import com.hms.hr_service.mapper.MedicalOrganizationMapper;
import com.hms.hr_service.mapper.WorkforceMapper;
import com.hms.hr_service.messaging.AnalyticsEventPublisher;
import com.hms.hr_service.messaging.NotificationEventPublisher;
import com.hms.hr_service.model.*;
import com.hms.hr_service.repository.*;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.security.HrAccessService;
import com.hms.hr_service.specification.WorkforceSpecification;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
  private final AnalyticsEventPublisher analyticsEventPublisher;
  private final NotificationEventPublisher notificationEventPublisher;
  private final HrAccessService hrAccessService;
  private final MedicalOrganizationRepository organizationRepository;
  private final MedicalOrganizationMapper organizationMapper;
  private final WorkforceMapper workforceMapper;

  public UUID departmentOrganizationId(UUID departmentId) {
    return department(departmentId).getMedicalOrganization().getId();
  }

  public UUID leaveEmployeeId(UUID leaveId) {
    return leaveRepository
        .findById(leaveId)
        .orElseThrow(() -> new IllegalArgumentException("Leave request not found"))
        .getEmployee()
        .getId();
  }

  @Transactional(readOnly = true)
  public List<MedicalOrganizationResponseDTO> managedOrganizations(
      AuthenticatedPrincipal principal) {
    var organizations =
        principal.role() == UserRole.ADMIN
            ? organizationRepository.findAll()
            : organizationRepository.findAllById(hrAccessService.managedOrganizationIds(principal));
    return organizations.stream()
        .map(organizationMapper::medicalOrganizationToMedicalOrganizationResponseDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<WorkforceDTO.ShiftResponse> listShifts(
      UUID organizationId,
      UUID employeeId,
      LocalDate from,
      LocalDate to,
      Pageable pageable,
      AuthenticatedPrincipal principal) {
    requireManagementScope(principal, organizationId, employeeId);
    return PageResponse.from(
        shiftRepository
            .findAll(WorkforceSpecification.shifts(employeeId, organizationId, from, to), pageable)
            .map(workforceMapper::shift));
  }

  @Transactional(readOnly = true)
  public PageResponse<WorkforceDTO.LeaveResponse> listLeave(
      UUID organizationId,
      UUID employeeId,
      String status,
      Pageable pageable,
      AuthenticatedPrincipal principal) {
    requireManagementScope(principal, organizationId, employeeId);
    return PageResponse.from(
        leaveRepository
            .findAll(WorkforceSpecification.leave(employeeId, organizationId, status), pageable)
            .map(workforceMapper::leave));
  }

  @Transactional(readOnly = true)
  public PageResponse<WorkforceDTO.AttendanceResponse> listAttendance(
      UUID organizationId,
      UUID employeeId,
      LocalDate from,
      LocalDate to,
      Pageable pageable,
      AuthenticatedPrincipal principal) {
    requireManagementScope(principal, organizationId, employeeId);
    return PageResponse.from(
        attendanceRepository
            .findAll(
                WorkforceSpecification.attendance(employeeId, organizationId, from, to), pageable)
            .map(workforceMapper::attendance));
  }

  private void requireManagementScope(
      AuthenticatedPrincipal principal, UUID organizationId, UUID employeeId) {
    if (principal.role() == UserRole.HR_HEAD) {
      if (organizationId == null) throw new AccessDeniedException("Organization required");
      hrAccessService.requireOrganization(principal, organizationId);
      if (employeeId != null) hrAccessService.requireEmployee(principal, employeeId);
    }
  }

  @Transactional
  public WorkforceDTO.CredentialResponse addCredential(
      UUID employeeId, WorkforceDTO.CredentialRequest request) {
    Employee employee = employee(employeeId);
    EmployeeCredential credential = new EmployeeCredential();
    credential.setEmployee(employee);
    credential.setCredentialType(request.credentialType());
    credential.setCredentialNumber(request.credentialNumber());
    credential.setExpiresOn(request.expiresOn());
    return workforceMapper.credential(credentialRepository.save(credential));
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.CredentialResponse> expiringCredentials(int days) {
    LocalDate limit = LocalDate.now().plusDays(Math.max(days, 0));
    return credentialRepository.findByExpiresOnLessThanEqual(limit).stream()
        .map(workforceMapper::credential)
        .toList();
  }

  @Transactional
  public WorkforceDTO.ShiftResponse createShift(WorkforceDTO.ShiftRequest request) {
    Employee employee = employee(request.employeeId());
    if (!request.startsAt().isBefore(request.endsAt())) {
      throw new IllegalArgumentException("Shift end must be after start");
    }
    EmployeeShift shift = new EmployeeShift();
    shift.setEmployee(employee);
    shift.setDepartment(request.departmentId() == null ? null : department(request.departmentId()));
    shift.setShiftDate(request.shiftDate());
    shift.setStartsAt(request.startsAt());
    shift.setEndsAt(request.endsAt());
    EmployeeShift saved = shiftRepository.save(shift);
    analyticsEventPublisher.publish("SHIFT_CREATED", "EMPLOYEE_SHIFT", saved.getId());
    notificationEventPublisher.publish(
        employee.getUserId(),
        "New shift assigned",
        "A shift has been added to your schedule for " + saved.getShiftDate() + ".");
    return workforceMapper.shift(saved);
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.ShiftResponse> shifts(UUID employeeId, LocalDate from, LocalDate to) {
    employee(employeeId);
    return shiftRepository.findByEmployeeIdAndShiftDateBetween(employeeId, from, to).stream()
        .map(workforceMapper::shift)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<WorkforceDTO.ShiftResponse> availability(LocalDate date) {
    return shiftRepository.findByShiftDate(date).stream().map(workforceMapper::shift).toList();
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
    LeaveRequest saved = leaveRepository.save(leave);
    analyticsEventPublisher.publish("LEAVE_REQUESTED", "LEAVE_REQUEST", saved.getId());
    notificationEventPublisher.publish(
        employee.getUserId(), "Leave request submitted", "Your leave request is awaiting review.");
    return workforceMapper.leave(saved);
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
    boolean changed = !status.equals(leave.getStatus());
    leave.setStatus(status);
    leave.setDecisionReason(request.decisionReason());
    LeaveRequest saved = leaveRepository.save(leave);
    if (changed) {
      analyticsEventPublisher.publish("LEAVE_" + status, "LEAVE_REQUEST", saved.getId());
      notificationEventPublisher.publish(
          leave.getEmployee().getUserId(),
          "Leave decision",
          "Your leave request has been " + status.toLowerCase(java.util.Locale.ROOT) + ".");
    }
    return workforceMapper.leave(saved);
  }

  @Transactional
  public WorkforceDTO.AttendanceResponse recordAttendance(WorkforceDTO.AttendanceRequest request) {
    Employee employee = employee(request.employeeId());
    AttendanceRecord record =
        attendanceRepository
            .findByEmployeeIdAndAttendanceDate(request.employeeId(), request.attendanceDate())
            .orElseGet(AttendanceRecord::new);
    boolean checkedIn = request.checkIn() && record.getCheckedInAt() == null;
    boolean checkedOut = request.checkOut() && record.getCheckedOutAt() == null;
    record.setEmployee(employee);
    record.setAttendanceDate(request.attendanceDate());
    if (checkedIn) record.setCheckedInAt(LocalDateTime.now());
    if (checkedOut) record.setCheckedOutAt(LocalDateTime.now());
    AttendanceRecord saved = attendanceRepository.save(record);
    if (checkedIn) {
      analyticsEventPublisher.publish("ATTENDANCE_CHECKED_IN", "ATTENDANCE_RECORD", saved.getId());
    }
    if (checkedOut) {
      analyticsEventPublisher.publish("ATTENDANCE_CHECKED_OUT", "ATTENDANCE_RECORD", saved.getId());
    }
    return workforceMapper.attendance(saved);
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
}
