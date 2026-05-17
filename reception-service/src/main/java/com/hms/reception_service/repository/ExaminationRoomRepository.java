package com.hms.reception_service.repository;

import com.hms.reception_service.model.ExaminationRoom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExaminationRoomRepository extends JpaRepository<ExaminationRoom, UUID> {
  Optional<ExaminationRoom> findByRoomCode(String roomCode);

  boolean existsByRoomCode(String roomCode);

  List<ExaminationRoom> findByDepartmentIdAndIsActiveTrue(UUID departmentId);

  List<ExaminationRoom> findByIsActiveTrue();
}
