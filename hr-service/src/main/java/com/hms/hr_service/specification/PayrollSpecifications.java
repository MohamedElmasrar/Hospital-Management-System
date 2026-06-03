package com.hms.hr_service.specification;

import com.hms.hr_service.enums.PayrollStatus;
import com.hms.hr_service.model.*;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PayrollSpecifications {
  private PayrollSpecifications() {}

  public static Specification<PayrollRecord> records(
      UUID organizationId,
      UUID employeeId,
      LocalDate period,
      PayrollStatus status,
      boolean publishedOnly) {
    return (root, query, cb) ->
        cb.and(
            organizationId == null
                ? cb.conjunction()
                : cb.equal(root.get("organizationId"), organizationId),
            employeeId == null
                ? cb.conjunction()
                : cb.equal(root.get("employee").get("id"), employeeId),
            period == null ? cb.conjunction() : cb.equal(root.get("period"), period),
            status == null ? cb.conjunction() : cb.equal(root.get("status"), status),
            publishedOnly
                ? cb.notEqual(root.get("status"), PayrollStatus.DRAFT)
                : cb.conjunction());
  }

  public static Specification<EmployeeAssignment> assigned(UUID employeeId, UUID organizationId) {
    return (root, query, cb) ->
        cb.and(
            cb.equal(root.get("employee").get("id"), employeeId),
            cb.equal(root.get("department").get("medicalOrganization").get("id"), organizationId));
  }
}
