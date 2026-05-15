package com.hms.billing_service.repository;

import com.hms.billing_service.enums.ClaimStatus;
import com.hms.billing_service.model.InsuranceClaim;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {
  Optional<InsuranceClaim> findByInvoiceId(UUID invoiceId);

  boolean existsByInvoiceId(UUID invoiceId);

  Page<InsuranceClaim> findByStatus(ClaimStatus status, Pageable pageable);

  Page<InsuranceClaim> findByPolicyPatientId(UUID patientId, Pageable pageable);
}

