package com.hms.reception_service.specification;

import com.hms.reception_service.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecifications {
  private ReservationSpecifications() {}

  public static Specification<Reservation> search(String query, LocalDate date) {
    List<Specification<Reservation>> filters = new ArrayList<>();

    if (query != null && !query.isBlank()) {
      String pattern = "%" + query.trim().toLowerCase() + "%";
      filters.add(
          (root, criteriaQuery, builder) -> {
            var patient = root.join("patient");
            return builder.or(
                builder.like(builder.lower(root.get("reservationNumber")), pattern),
                builder.like(builder.lower(patient.get("firstName")), pattern),
                builder.like(builder.lower(patient.get("lastName")), pattern),
                builder.like(builder.lower(patient.get("phone")), pattern));
          });
    }

    if (date != null) {
      filters.add(
          (root, criteriaQuery, builder) -> builder.equal(root.get("reservationDate"), date));
    }

    return Specification.allOf(filters);
  }

  public static Specification<Reservation> conflictingPractitioner(
      UUID practitionerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
    return conflictingSlot(date, startTime, endTime)
        .and((root, query, builder) -> builder.equal(root.get("practitionerId"), practitionerId));
  }

  public static Specification<Reservation> conflictingRoom(
      UUID roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
    return conflictingSlot(date, startTime, endTime)
        .and((root, query, builder) -> builder.equal(root.get("room").get("id"), roomId));
  }

  private static Specification<Reservation> conflictingSlot(
      LocalDate date, LocalTime startTime, LocalTime endTime) {
    return (root, query, builder) ->
        builder.and(
            builder.equal(root.get("reservationDate"), date),
            builder.isFalse(root.get("status").get("isTerminal")),
            builder.lessThan(root.get("startTime"), endTime),
            builder.greaterThan(root.get("endTime"), startTime));
  }

  public static Specification<Reservation> doctorConsultations(
      UUID practitionerId, String searchTerm, LocalDate date) {
    List<Specification<Reservation>> filters = new ArrayList<>();
    filters.add(
        (root, query, builder) -> builder.equal(root.get("practitionerId"), practitionerId));
    filters.add(
        (root, query, builder) -> builder.equal(root.get("category").get("code"), "CONSULTATION"));

    if (searchTerm != null && !searchTerm.isBlank()) {
      String pattern = "%" + searchTerm.trim().toLowerCase() + "%";
      filters.add(
          (root, query, builder) -> {
            var patient = root.join("patient");
            return builder.or(
                builder.like(builder.lower(root.get("reservationNumber")), pattern),
                builder.like(builder.lower(patient.get("firstName")), pattern),
                builder.like(builder.lower(patient.get("lastName")), pattern),
                builder.like(builder.lower(patient.get("phone")), pattern));
          });
    }

    if (date != null) {
      filters.add((root, query, builder) -> builder.equal(root.get("reservationDate"), date));
    }

    return Specification.allOf(filters);
  }
}
