package com.hms.hr_service.repository;

import com.hms.hr_service.model.Employee;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository
    extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

  Optional<Employee> findByIdentifier(String identifier);

  Optional<Employee> findByUserId(UUID userId);

  boolean existsByIdentifier(String identifier);
}
