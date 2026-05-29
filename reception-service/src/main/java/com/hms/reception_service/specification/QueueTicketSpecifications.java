package com.hms.reception_service.specification;

import com.hms.reception_service.model.QueueTicket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class QueueTicketSpecifications {
  private QueueTicketSpecifications() {}

  public static Specification<QueueTicket> search(LocalDate date, String query) {
    List<Specification<QueueTicket>> filters = new ArrayList<>();
    filters.add(
        (root, criteriaQuery, builder) -> builder.equal(root.join("queue").get("queueDate"), date));

    if (query != null && !query.isBlank()) {
      String pattern = "%" + query.trim().toLowerCase() + "%";
      filters.add(
          (root, criteriaQuery, builder) -> {
            var reservation = root.join("reservation");
            var patient = reservation.join("patient");
            var room = reservation.join("room");
            return builder.or(
                builder.like(builder.lower(root.get("ticketCode")), pattern),
                builder.like(builder.lower(patient.get("firstName")), pattern),
                builder.like(builder.lower(patient.get("lastName")), pattern),
                builder.like(builder.lower(room.get("name")), pattern));
          });
    }

    return Specification.allOf(filters);
  }
}
