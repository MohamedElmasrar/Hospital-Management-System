package com.hms.hr_service.repository;

import com.hms.hr_service.model.EmployeeCompensation;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface EmployeeCompensationRepository
    extends JpaRepository<EmployeeCompensation, UUID>,
        JpaSpecificationExecutor<EmployeeCompensation> {
  Optional<EmployeeCompensation> findByOrganizationIdAndEmployeeId(
      UUID organizationId, UUID employeeId);
}
