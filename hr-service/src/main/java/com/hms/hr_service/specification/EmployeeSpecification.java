package com.hms.hr_service.specification;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.model.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

  private EmployeeSpecification() {}

  public static Specification<Employee> hasSearch(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + search.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("givenName")), pattern),
          cb.like(cb.lower(root.get("familyName")), pattern),
          cb.like(cb.lower(root.get("identifier")), pattern));
    };
  }

  public static Specification<Employee> hasEmploymentStatus(EmploymentStatus status) {
    return (root, query, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("employmentStatus"), status);
    };
  }
}
