package com.hms.reception_service.repository;

import com.hms.reception_service.model.Admission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdmissionRepository
    extends JpaRepository<Admission, UUID>, JpaSpecificationExecutor<Admission> {
  Optional<Admission> findByBedIdAndDischargedAtIsNull(UUID bedId);

  Optional<Admission> findByPatientIdAndDischargedAtIsNull(UUID patientId);
}
