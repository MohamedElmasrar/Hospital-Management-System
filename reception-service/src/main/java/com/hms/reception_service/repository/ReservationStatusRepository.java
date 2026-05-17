package com.hms.reception_service.repository;

import com.hms.reception_service.model.ReservationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationStatusRepository extends JpaRepository<ReservationStatus, UUID> {
  Optional<ReservationStatus> findByCode(String code);

  boolean existsByCode(String code);

  List<ReservationStatus> findByIsActiveTrue();
}
