package com.hms.hr_service.repository;

import com.hms.hr_service.model.PayrollRecord;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PayrollRecordRepository
    extends JpaRepository<PayrollRecord, UUID>, JpaSpecificationExecutor<PayrollRecord> {
  boolean existsByOrganizationIdAndEmployeeIdAndPeriod(
      UUID organizationId, UUID employeeId, LocalDate period);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PayrollRecord> findOneById(UUID id);
}
