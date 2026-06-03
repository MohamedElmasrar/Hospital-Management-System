package com.hms.hr_service.repository;

import com.hms.hr_service.model.EmployeeAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeAssignmentRepository
    extends JpaRepository<EmployeeAssignment, UUID>, JpaSpecificationExecutor<EmployeeAssignment> {

  List<EmployeeAssignment> findByEmployeeId(UUID employeeId);

  List<EmployeeAssignment> findByDepartmentId(UUID departmentId);
}
