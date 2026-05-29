package com.hms.reception_service.specification;

import com.hms.reception_service.model.Reservation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
}
