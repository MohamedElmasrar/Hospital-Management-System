package com.hms.reception_service.repository;

import com.hms.reception_service.model.DiagnosticPrescription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticPrescriptionRepository
    extends JpaRepository<DiagnosticPrescription, UUID> {
  Optional<DiagnosticPrescription> findByPrescriptionCode(String prescriptionCode);

  List<DiagnosticPrescription> findByPatientIdAndIsFulfilledFalse(UUID patientId);

  List<DiagnosticPrescription> findByPatientId(UUID patientId);

  List<DiagnosticPrescription> findByModalityIdAndIsFulfilledFalse(UUID modalityId);
}
