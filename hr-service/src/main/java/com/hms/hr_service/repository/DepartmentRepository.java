package com.hms.hr_service.repository;

import com.hms.hr_service.model.Department;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository
    extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {

  Optional<Department> findByIdAndMedicalOrganizationId(UUID id, UUID medicalOrganizationId);

  Page<Department> findByMedicalOrganizationId(UUID medicalOrganizationId, Pageable pageable);

  List<Department> findByMedicalOrganizationId(UUID medicalOrganizationId);

  List<Department> findByParentDepartmentId(UUID parentDepartmentId);
}
