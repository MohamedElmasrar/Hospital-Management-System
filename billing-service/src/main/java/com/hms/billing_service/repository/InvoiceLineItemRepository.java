package com.hms.billing_service.repository;

import com.hms.billing_service.model.InvoiceLineItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {
  boolean existsByBillableOrderId(UUID billableOrderId);

  Optional<InvoiceLineItem> findByBillableOrderId(UUID billableOrderId);

  List<InvoiceLineItem> findByInvoiceId(UUID invoiceId);
}
