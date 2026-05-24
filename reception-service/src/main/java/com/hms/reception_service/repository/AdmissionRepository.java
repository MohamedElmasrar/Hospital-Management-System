package com.hms.reception_service.repository;

import com.hms.reception_service.model.Admission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRepository extends JpaRepository<Admission, UUID> {
  Optional<Admission> findByBedIdAndDischargedAtIsNull(UUID bedId);
  Optional<Admission> findByPatientIdAndDischargedAtIsNull(UUID patientId);
}
