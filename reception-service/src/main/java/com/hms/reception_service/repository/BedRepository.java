package com.hms.reception_service.repository;

import com.hms.reception_service.model.Bed;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedRepository extends JpaRepository<Bed, UUID> {
  List<Bed> findByOccupiedFalse();
}
