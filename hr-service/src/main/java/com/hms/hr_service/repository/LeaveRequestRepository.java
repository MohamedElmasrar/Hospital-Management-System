package com.hms.hr_service.repository;

import com.hms.hr_service.model.LeaveRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaveRequestRepository
    extends JpaRepository<LeaveRequest, UUID>, JpaSpecificationExecutor<LeaveRequest> {
  List<LeaveRequest> findByEmployeeId(UUID employeeId);
}
