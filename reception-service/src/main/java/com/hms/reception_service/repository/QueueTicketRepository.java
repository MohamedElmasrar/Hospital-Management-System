package com.hms.reception_service.repository;

import com.hms.reception_service.model.QueueTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QueueTicketRepository
    extends JpaRepository<QueueTicket, UUID>, JpaSpecificationExecutor<QueueTicket> {
  Optional<QueueTicket> findByReservationId(UUID reservationId);

  List<QueueTicket> findByQueueIdAndStatus(UUID queueId, String status);
}
