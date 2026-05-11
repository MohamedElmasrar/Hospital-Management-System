package com.hms.hr_service.specification;

import com.hms.hr_service.model.Physician;
import org.springframework.data.jpa.domain.Specification;

public class PhysicianSpecification {

  private PhysicianSpecification() {}

  public static Specification<Physician> hasSearch(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + search.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("licenseNumber")), pattern),
          cb.like(cb.lower(root.get("employee").get("identifier")), pattern),
          cb.like(cb.lower(root.get("employee").get("givenName")), pattern),
          cb.like(cb.lower(root.get("employee").get("familyName")), pattern));
    };
  }
}
