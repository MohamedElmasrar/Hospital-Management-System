package com.hms.hr_service.repository;

import com.hms.hr_service.model.AttendanceRecord;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
  Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate date);
}
