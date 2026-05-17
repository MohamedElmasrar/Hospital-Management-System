package com.hms.reception_service.repository;

import com.hms.reception_service.model.DailyQueue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyQueueRepository extends JpaRepository<DailyQueue, UUID> {
  Optional<DailyQueue> findByDepartmentIdAndRoomIdAndQueueDate(
      UUID departmentId, UUID roomId, LocalDate queueDate);

  Optional<DailyQueue> findByRoomIdAndQueueDate(UUID roomId, LocalDate queueDate);

  List<DailyQueue> findByDepartmentIdAndQueueDate(UUID departmentId, LocalDate queueDate);
}
