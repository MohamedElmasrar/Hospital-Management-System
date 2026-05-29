package com.hms.reception_service.specification;

import com.hms.reception_service.model.Admission;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AdmissionSpecifications {
  private AdmissionSpecifications() {}

  public static Specification<Admission> search(String query, boolean activeOnly) {
    List<Specification<Admission>> filters = new ArrayList<>();

    if (query != null && !query.isBlank()) {
      String pattern = "%" + query.trim().toLowerCase() + "%";
      filters.add(
          (root, criteriaQuery, builder) -> {
            var patient = root.join("patient");
            var bed = root.join("bed");
            return builder.or(
                builder.like(builder.lower(patient.get("firstName")), pattern),
                builder.like(builder.lower(patient.get("lastName")), pattern),
                builder.like(builder.lower(bed.get("bedCode")), pattern),
                builder.like(builder.lower(bed.get("ward")), pattern));
          });
    }

    if (activeOnly) {
      filters.add((root, criteriaQuery, builder) -> builder.isNull(root.get("dischargedAt")));
    }

    return Specification.allOf(filters);
  }
}
