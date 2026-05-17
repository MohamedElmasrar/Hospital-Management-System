package com.hms.reception_service.repository;

import com.hms.reception_service.model.DiagnosticModality;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticModalityRepository extends JpaRepository<DiagnosticModality, UUID> {
  Optional<DiagnosticModality> findByCode(String code);

  boolean existsByCode(String code);

  List<DiagnosticModality> findByDepartmentIdAndIsActiveTrue(UUID departmentId);

  List<DiagnosticModality> findByIsActiveTrue();
}
