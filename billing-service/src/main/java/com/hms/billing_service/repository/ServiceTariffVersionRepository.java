package com.hms.billing_service.repository;

import com.hms.billing_service.model.ServiceTariffVersion;
import com.hms.billing_service.specification.ServiceTariffVersionSpecifications;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceTariffVersionRepository
    extends JpaRepository<ServiceTariffVersion, UUID>,
        JpaSpecificationExecutor<ServiceTariffVersion> {

  List<ServiceTariffVersion> findByCatalogItemIdOrderByEffectiveFromDesc(UUID catalogItemId);

  default Optional<ServiceTariffVersion> findActiveTariff(UUID catalogItemId, Date date) {
    List<ServiceTariffVersion> list =
        findAll(
            ServiceTariffVersionSpecifications.activeOn(catalogItemId, date),
            Sort.by(Sort.Direction.DESC, "effectiveFrom"));
    return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
  }

  Optional<ServiceTariffVersion>
      findFirstByCatalogItemIdAndEffectiveToIsNullOrderByEffectiveFromDesc(UUID catalogItemId);
}
