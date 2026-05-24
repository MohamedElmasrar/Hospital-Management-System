package com.hms.hr_service.repository;

import com.hms.hr_service.model.EmployeeCredential;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeCredentialRepository extends JpaRepository<EmployeeCredential, UUID> {
  List<EmployeeCredential> findByEmployeeId(UUID employeeId);
  List<EmployeeCredential> findByExpiresOnLessThanEqual(LocalDate date);
}
