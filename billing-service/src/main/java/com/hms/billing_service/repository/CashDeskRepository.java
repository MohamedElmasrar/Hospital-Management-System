package com.hms.billing_service.repository;

import com.hms.billing_service.model.CashDesk;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashDeskRepository extends JpaRepository<CashDesk, UUID> {
  Optional<CashDesk> findByDeskCode(String deskCode);

  boolean existsByDeskCode(String deskCode);

  Page<CashDesk> findByDepartmentId(UUID departmentId, Pageable pageable);

  List<CashDesk> findByDepartmentIdAndIsActiveTrue(UUID departmentId);
}
