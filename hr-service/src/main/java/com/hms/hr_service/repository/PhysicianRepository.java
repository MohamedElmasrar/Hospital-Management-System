package com.hms.hr_service.repository;

import com.hms.hr_service.model.Physician;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PhysicianRepository
    extends JpaRepository<Physician, UUID>, JpaSpecificationExecutor<Physician> {

  Optional<Physician> findByLicenseNumber(String licenseNumber);

  Optional<Physician> findByEmployeeId(UUID employeeId);

  boolean existsByLicenseNumber(String licenseNumber);

  boolean existsByEmployeeId(UUID employeeId);
}
