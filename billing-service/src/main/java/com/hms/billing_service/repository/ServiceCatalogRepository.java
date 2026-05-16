package com.hms.billing_service.repository;

import com.hms.billing_service.model.ServiceCatalog;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID> {
  Optional<ServiceCatalog> findByServiceCode(String serviceCode);

  boolean existsByServiceCode(String serviceCode);

  Optional<ServiceCatalog> findByExternalServiceId(UUID externalServiceId);

  Page<ServiceCatalog> findByDepartmentId(UUID departmentId, Pageable pageable);

  List<ServiceCatalog> findByDepartmentIdAndIsActiveTrue(UUID departmentId);
}
