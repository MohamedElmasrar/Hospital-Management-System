package com.hms.reception_service.repository;

import com.hms.reception_service.model.ReservationCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCategoryRepository extends JpaRepository<ReservationCategory, UUID> {
  Optional<ReservationCategory> findByCode(String code);

  boolean existsByCode(String code);

  List<ReservationCategory> findByIsActiveTrue();
}
