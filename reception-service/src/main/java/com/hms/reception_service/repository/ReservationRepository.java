package com.hms.reception_service.repository;

import com.hms.reception_service.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
  Optional<Reservation> findByReservationNumber(String reservationNumber);

  Page<Reservation> findByPatientId(UUID patientId, Pageable pageable);

  List<Reservation> findByReservationDateAndDepartmentId(
      LocalDate reservationDate, UUID departmentId);

  List<Reservation> findByReservationDateAndPractitionerId(
      LocalDate reservationDate, UUID practitionerId);

  List<Reservation> findByReservationDateAndRoomId(LocalDate reservationDate, UUID roomId);

  @Query(
      "SELECT r FROM Reservation r WHERE r.practitionerId = :practitionerId "
          + "AND r.reservationDate = :date "
          + "AND r.status.isTerminal = false "
          + "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
  List<Reservation> findConflictingPractitionerReservations(
      @Param("practitionerId") UUID practitionerId,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query(
      "SELECT r FROM Reservation r WHERE r.room.id = :roomId "
          + "AND r.reservationDate = :date "
          + "AND r.status.isTerminal = false "
          + "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
  List<Reservation> findConflictingRoomReservations(
      @Param("roomId") UUID roomId,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);
}
