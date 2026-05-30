package com.hms.billing_service.repository;

import com.hms.billing_service.enums.PaymentStatus;
import com.hms.billing_service.model.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByPaymentReference(String paymentReference);

  boolean existsByPaymentReference(String paymentReference);

  Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

  List<Payment> findByInvoiceId(UUID invoiceId);

  List<Payment> findByInvoiceIdAndStatus(UUID invoiceId, PaymentStatus status);

  Page<Payment> findByCashDeskId(UUID cashDeskId, Pageable pageable);
}
