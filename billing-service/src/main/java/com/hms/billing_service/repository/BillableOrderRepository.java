package com.hms.billing_service.repository;

import com.hms.billing_service.enums.SourceOrderType;
import com.hms.billing_service.model.BillableOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillableOrderRepository extends JpaRepository<BillableOrder, UUID> {
  boolean existsByExternalOrderIdAndOrderType(UUID externalOrderId, SourceOrderType orderType);

  Optional<BillableOrder> findByExternalOrderIdAndOrderType(
      UUID externalOrderId, SourceOrderType orderType);

  Page<BillableOrder> findByPatientId(UUID patientId, Pageable pageable);

  Page<BillableOrder> findByPractitionerId(UUID practitionerId, Pageable pageable);
}
