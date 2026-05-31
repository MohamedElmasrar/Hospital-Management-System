package com.hms.hr_service.specification;

import com.hms.hr_service.model.AttendanceRecord;
import com.hms.hr_service.model.EmployeeAssignment;
import com.hms.hr_service.model.EmployeeShift;
import com.hms.hr_service.model.LeaveRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class WorkforceSpecification {
  private WorkforceSpecification() {}

  public static Specification<EmployeeShift> shifts(
      UUID employeeId, UUID organizationId, LocalDate from, LocalDate to) {
    return (root, query, cb) ->
        cb.and(
            employeeId == null
                ? cb.conjunction()
                : cb.equal(root.get("employee").get("id"), employeeId),
            organizationId == null
                ? cb.conjunction()
                : cb.equal(
                    root.get("department").get("medicalOrganization").get("id"), organizationId),
            cb.between(root.get("shiftDate"), from, to));
  }

  public static Specification<LeaveRequest> leave(
      UUID employeeId, UUID organizationId, String status) {
    return (root, query, cb) -> {
      var predicates = new ArrayList<Predicate>();
      if (employeeId != null) predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
      if (organizationId != null) {
        query.distinct(true);
        Subquery<UUID> assigned = query.subquery(UUID.class);
        Root<EmployeeAssignment> assignment = assigned.from(EmployeeAssignment.class);
        assigned.select(assignment.get("employee").get("id"));
        assigned.where(activeOrganization(cb, assignment, organizationId));
        predicates.add(root.get("employee").get("id").in(assigned));
      }
      if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status));
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  public static Specification<AttendanceRecord> attendance(
      UUID employeeId, UUID organizationId, LocalDate from, LocalDate to) {
    return (root, query, cb) -> {
      var predicates = new ArrayList<Predicate>();
      if (employeeId != null) predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
      if (organizationId != null) {
        query.distinct(true);
        Subquery<UUID> assigned = query.subquery(UUID.class);
        Root<EmployeeAssignment> assignment = assigned.from(EmployeeAssignment.class);
        assigned.select(assignment.get("employee").get("id"));
        assigned.where(activeOrganization(cb, assignment, organizationId));
        predicates.add(root.get("employee").get("id").in(assigned));
      }
      predicates.add(cb.between(root.get("attendanceDate"), from, to));
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private static Predicate activeOrganization(
      CriteriaBuilder cb, Root<EmployeeAssignment> assignment, UUID organizationId) {
    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date tomorrow =
        Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    return cb.and(
        cb.equal(assignment.get("department").get("medicalOrganization").get("id"), organizationId),
        cb.lessThan(assignment.get("startDate"), tomorrow),
        cb.or(
            cb.isNull(assignment.get("endDate")),
            cb.greaterThanOrEqualTo(assignment.get("endDate"), today)));
  }
}
