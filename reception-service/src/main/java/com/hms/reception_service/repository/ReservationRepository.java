package com.hms.reception_service.repository;

import com.hms.reception_service.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservationRepository
    extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {
  Optional<Reservation> findByReservationNumber(String reservationNumber);

  Page<Reservation> findByPatientId(UUID patientId, Pageable pageable);

  List<Reservation> findByReservationDateAndDepartmentId(
      LocalDate reservationDate, UUID departmentId);

  List<Reservation> findByReservationDateAndPractitionerId(
      LocalDate reservationDate, UUID practitionerId);

  List<Reservation> findByReservationDateAndRoomId(LocalDate reservationDate, UUID roomId);
}
