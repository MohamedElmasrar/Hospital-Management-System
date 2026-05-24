package com.hms.auth_service.repository;

import com.hms.auth_service.model.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  List<AuditLog> findAllByOrderByCreatedAtDesc();
}
