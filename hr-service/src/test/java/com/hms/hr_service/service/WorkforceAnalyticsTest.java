package com.hms.hr_service.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hms.hr_service.dto.workforce.WorkforceDTO;
import com.hms.hr_service.messaging.AnalyticsEventPublisher;
import com.hms.hr_service.model.AttendanceRecord;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.repository.AttendanceRecordRepository;
import com.hms.hr_service.repository.DepartmentRepository;
import com.hms.hr_service.repository.EmployeeCredentialRepository;
import com.hms.hr_service.repository.EmployeeRepository;
import com.hms.hr_service.repository.EmployeeShiftRepository;
import com.hms.hr_service.repository.LeaveRequestRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkforceAnalyticsTest {
  @Mock private EmployeeRepository employeeRepository;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private EmployeeCredentialRepository credentialRepository;
  @Mock private EmployeeShiftRepository shiftRepository;
  @Mock private LeaveRequestRepository leaveRepository;
  @Mock private AttendanceRecordRepository attendanceRepository;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;
  @InjectMocks private WorkforceService workforceService;

  @Test
  void repeatedCheckInIsCountedOnce() {
    UUID employeeId = UUID.randomUUID();
    LocalDate date = LocalDate.of(2026, 10, 30);
    Employee employee = new Employee();
    employee.setId(employeeId);
    AttendanceRecord record = new AttendanceRecord();
    record.setId(UUID.randomUUID());
    when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
    when(attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date))
        .thenReturn(Optional.of(record));
    when(attendanceRepository.save(record)).thenReturn(record);
    WorkforceDTO.AttendanceRequest request =
        new WorkforceDTO.AttendanceRequest(employeeId, date, true, false);

    workforceService.recordAttendance(request);
    workforceService.recordAttendance(request);

    verify(analyticsEventPublisher)
        .publish("ATTENDANCE_CHECKED_IN", "ATTENDANCE_RECORD", record.getId());
  }
}
