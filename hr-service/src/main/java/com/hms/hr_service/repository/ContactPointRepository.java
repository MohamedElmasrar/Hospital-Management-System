package com.hms.hr_service.repository;

import com.hms.hr_service.model.ContactPoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactPointRepository extends JpaRepository<ContactPoint, UUID> {

  List<ContactPoint> findByEmployeeId(UUID employeeId);
}
