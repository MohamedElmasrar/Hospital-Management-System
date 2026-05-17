package com.hms.reception_service.repository;

import com.hms.reception_service.model.QueuePriorityLevel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueuePriorityLevelRepository extends JpaRepository<QueuePriorityLevel, UUID> {
  Optional<QueuePriorityLevel> findByCode(String code);

  boolean existsByCode(String code);

  List<QueuePriorityLevel> findByIsActiveTrueOrderByWeightDesc();
}
