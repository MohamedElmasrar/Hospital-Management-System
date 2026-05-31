package com.hms.reception_service.repository;

import com.hms.reception_service.model.Patient;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository
    extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {
  Optional<Patient> findByMrn(String mrn);

  Optional<Patient> findByNationalId(String nationalId);

  boolean existsByNationalId(String nationalId);

  boolean existsByMrn(String mrn);
}
