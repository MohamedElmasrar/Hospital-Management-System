package com.hms.billing_service.specification;

import com.hms.billing_service.model.ServiceTariffVersion;
import java.util.Date;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ServiceTariffVersionSpecifications {

  private ServiceTariffVersionSpecifications() {}

  public static Specification<ServiceTariffVersion> activeOn(UUID catalogItemId, Date date) {
    return (root, query, builder) ->
        builder.and(
            builder.equal(root.get("catalogItem").get("id"), catalogItemId),
            builder.lessThanOrEqualTo(root.get("effectiveFrom"), date),
            builder.or(
                builder.isNull(root.get("effectiveTo")),
                builder.greaterThan(root.get("effectiveTo"), date)));
  }
}
