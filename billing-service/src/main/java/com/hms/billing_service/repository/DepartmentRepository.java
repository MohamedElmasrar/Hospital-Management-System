package com.hms.billing_service.repository;

import com.hms.billing_service.enums.BillingSectionType;
import com.hms.billing_service.model.Department;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
  Optional<Department> findByName(String name);

  boolean existsByName(String name);

  Page<Department> findBySection(BillingSectionType section, Pageable pageable);
}

