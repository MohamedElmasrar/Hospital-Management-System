package com.hms.billing_service.repository;

import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.model.Invoice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  boolean existsByInvoiceNumber(String invoiceNumber);

  Page<Invoice> findByPatientId(UUID patientId, Pageable pageable);

  Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

  Page<Invoice> findByPatientIdAndStatus(UUID patientId, InvoiceStatus status, Pageable pageable);
}

