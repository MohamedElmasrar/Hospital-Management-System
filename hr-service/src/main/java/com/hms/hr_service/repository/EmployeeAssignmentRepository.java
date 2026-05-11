package com.hms.hr_service.repository;

import com.hms.hr_service.model.EmployeeAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {

  List<EmployeeAssignment> findByEmployeeId(UUID employeeId);

  List<EmployeeAssignment> findByDepartmentId(UUID departmentId);
}
