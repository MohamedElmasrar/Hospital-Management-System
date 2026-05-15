package com.hms.billing_service.repository;

import com.hms.billing_service.model.ServiceTariffVersion;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTariffVersionRepository extends JpaRepository<ServiceTariffVersion, UUID> {

  List<ServiceTariffVersion> findByCatalogItemIdOrderByEffectiveFromDesc(UUID catalogItemId);

  @Query(
      "SELECT v FROM ServiceTariffVersion v WHERE v.catalogItem.id = :catalogItemId "
          + "AND v.effectiveFrom <= :date AND (v.effectiveTo IS NULL OR v.effectiveTo > :date) "
          + "ORDER BY v.effectiveFrom DESC")
  List<ServiceTariffVersion> findActiveTariffCandidates(
      @Param("catalogItemId") UUID catalogItemId, @Param("date") Date date);

  default Optional<ServiceTariffVersion> findActiveTariff(UUID catalogItemId, Date date) {
    List<ServiceTariffVersion> list = findActiveTariffCandidates(catalogItemId, date);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
  }

  Optional<ServiceTariffVersion>
      findFirstByCatalogItemIdAndEffectiveToIsNullOrderByEffectiveFromDesc(UUID catalogItemId);
}
