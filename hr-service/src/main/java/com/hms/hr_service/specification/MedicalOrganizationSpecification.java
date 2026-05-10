package com.hms.hr_service.specification;

import com.hms.hr_service.model.MedicalOrganization;
import org.springframework.data.jpa.domain.Specification;

public class MedicalOrganizationSpecification {
  private MedicalOrganizationSpecification() {}

  public static Specification<MedicalOrganization> hasSearch(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String namePattern = "%" + search.toLowerCase() + "%";
      return cb.like(cb.lower(root.get("name")), namePattern);
    };
  }

  public static Specification<MedicalOrganization> hasName(String name) {
    return (root, query, cb) -> {
      if (name == null || name.isBlank()) return cb.conjunction();
      return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    };
  }
}
