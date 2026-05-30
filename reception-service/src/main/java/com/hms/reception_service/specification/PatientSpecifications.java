package com.hms.reception_service.specification;

import com.hms.reception_service.model.Patient;
import com.hms.reception_service.model.Reservation;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PatientSpecifications {

  private PatientSpecifications() {}

  public static Specification<Patient> relatedToPractitioner(
      UUID practitionerId, String searchTerm) {
    return (root, query, criteriaBuilder) -> {
      var reservation = query.subquery(UUID.class);
      var reservationRoot = reservation.from(Reservation.class);
      reservation
          .select(reservationRoot.get("patient").get("id"))
          .where(
              criteriaBuilder.equal(
                  reservationRoot.get("practitionerId"), practitionerId),
              criteriaBuilder.equal(
                  reservationRoot.get("patient").get("id"), root.get("id")));

      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
      predicates.add(criteriaBuilder.exists(reservation));

      if (searchTerm != null && !searchTerm.isBlank()) {
        String pattern = "%" + searchTerm.trim().toLowerCase(Locale.ROOT) + "%";
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("mrn")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nationalId")), pattern),
                criteriaBuilder.like(root.get("phone"), pattern)));
      }

      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
