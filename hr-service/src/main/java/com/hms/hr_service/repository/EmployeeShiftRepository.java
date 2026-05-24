package com.hms.hr_service.repository;

import com.hms.hr_service.model.EmployeeShift;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, UUID> {
  List<EmployeeShift> findByEmployeeIdAndShiftDateBetween(
      UUID employeeId, LocalDate from, LocalDate to);

  List<EmployeeShift> findByShiftDate(LocalDate date);
}
