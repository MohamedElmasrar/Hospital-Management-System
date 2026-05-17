package com.hms.reception_service.repository;

import com.hms.reception_service.model.ReceptionStation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionStationRepository extends JpaRepository<ReceptionStation, UUID> {
  Optional<ReceptionStation> findByStationCode(String stationCode);

  boolean existsByStationCode(String stationCode);

  List<ReceptionStation> findByDepartmentIdAndIsActiveTrue(UUID departmentId);

  List<ReceptionStation> findByIsActiveTrue();
}
