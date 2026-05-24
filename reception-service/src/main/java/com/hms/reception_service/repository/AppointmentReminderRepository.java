package com.hms.reception_service.repository;

import com.hms.reception_service.model.AppointmentReminder;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, UUID> {
  List<AppointmentReminder> findByReservationId(UUID reservationId);
}
