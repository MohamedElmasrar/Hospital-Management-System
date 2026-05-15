package com.hms.billing_service.repository;

import com.hms.billing_service.model.InsuranceProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, UUID> {
  Optional<InsuranceProvider> findByCode(String code);

  boolean existsByCode(String code);

  Optional<InsuranceProvider> findByName(String name);

  boolean existsByName(String name);

  List<InsuranceProvider> findByIsActiveTrue();
}

