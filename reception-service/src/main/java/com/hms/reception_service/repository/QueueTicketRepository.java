package com.hms.reception_service.repository;

import com.hms.reception_service.model.QueueTicket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueTicketRepository
    extends JpaRepository<QueueTicket, UUID>, JpaSpecificationExecutor<QueueTicket> {
  Optional<QueueTicket> findByReservationId(UUID reservationId);

  List<QueueTicket> findByQueueIdAndStatus(UUID queueId, String status);

  @Query(
      "SELECT t FROM QueueTicket t WHERE t.queue.id = :queueId AND t.status = 'WAITING' "
          + "ORDER BY t.priority.weight DESC, t.sequenceNumber ASC")
  List<QueueTicket> findNextWaitingTickets(@Param("queueId") UUID queueId);

  @Query(
      "SELECT t FROM QueueTicket t WHERE t.queue.id = :queueId "
          + "ORDER BY t.priority.weight DESC, t.sequenceNumber ASC")
  List<QueueTicket> findAllQueueTickets(@Param("queueId") UUID queueId);
}
