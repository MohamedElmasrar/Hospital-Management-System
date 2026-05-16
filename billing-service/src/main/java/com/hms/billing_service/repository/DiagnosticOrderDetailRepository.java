package com.hms.billing_service.repository;

import com.hms.billing_service.model.DiagnosticOrderDetail;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticOrderDetailRepository
    extends JpaRepository<DiagnosticOrderDetail, UUID> {}
