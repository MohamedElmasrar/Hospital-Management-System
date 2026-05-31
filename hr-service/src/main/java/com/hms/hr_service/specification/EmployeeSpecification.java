package com.hms.hr_service.specification;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.EmployeeAssignment;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
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

  public static Specification<Employee> assignedToOrganization(UUID organizationId) {
    return (root, query, cb) -> {
      if (organizationId == null) return cb.conjunction();
      var assigned = query.subquery(UUID.class);
      var assignment = assigned.from(EmployeeAssignment.class);
      assigned.select(assignment.get("employee").get("id"));
      Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
      Date tomorrow =
          Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
      assigned.where(
          cb.equal(
              assignment.get("department").get("medicalOrganization").get("id"), organizationId),
          cb.lessThan(assignment.get("startDate"), tomorrow),
          cb.or(
              cb.isNull(assignment.get("endDate")),
              cb.greaterThanOrEqualTo(assignment.get("endDate"), today)));
      return root.get("id").in(assigned);
    };
  }
}
