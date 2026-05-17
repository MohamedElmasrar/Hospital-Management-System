package com.hms.reception_service.repository;

import com.hms.reception_service.model.ServiceSchedule;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceScheduleRepository extends JpaRepository<ServiceSchedule, UUID> {
  List<ServiceSchedule> findByPractitionerIdAndDayOfWeekAndIsActiveTrue(
      UUID practitionerId, DayOfWeek dayOfWeek);

  List<ServiceSchedule> findByRoomIdAndDayOfWeekAndIsActiveTrue(UUID roomId, DayOfWeek dayOfWeek);

  List<ServiceSchedule> findByDepartmentIdAndIsActiveTrue(UUID departmentId);

  List<ServiceSchedule> findByPractitionerIdAndIsActiveTrue(UUID practitionerId);
}
