package com.hms.reception_service.service;

import com.hms.reception_service.dto.PageResponse;
import com.hms.reception_service.dto.queue.QueueDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.mapper.QueueMapper;
import com.hms.reception_service.model.DailyQueue;
import com.hms.reception_service.model.ExaminationRoom;
import com.hms.reception_service.model.QueuePriorityLevel;
import com.hms.reception_service.model.QueueTicket;
import com.hms.reception_service.model.Reservation;
import com.hms.reception_service.repository.DailyQueueRepository;
import com.hms.reception_service.repository.QueuePriorityLevelRepository;
import com.hms.reception_service.repository.QueueTicketRepository;
import com.hms.reception_service.repository.ReservationRepository;
import com.hms.reception_service.repository.ReservationStatusRepository;
import com.hms.reception_service.specification.QueueTicketSpecifications;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

  private final DailyQueueRepository queueRepository;
  private final QueueTicketRepository ticketRepository;
  private final ReservationRepository reservationRepository;
  private final QueuePriorityLevelRepository priorityRepository;
  private final ReservationStatusRepository statusRepository;
  private final QueueMapper queueMapper;

  @Transactional
  public QueueDTO.DailyQueueResponse getOrCreateDailyQueue(UUID departmentId, UUID roomId) {
    LocalDate today = LocalDate.now();
    DailyQueue queue =
        queueRepository
            .findByDepartmentIdAndRoomIdAndQueueDate(departmentId, roomId, today)
            .orElseGet(
                () -> {
                  DailyQueue newQueue = new DailyQueue();
                  newQueue.setDepartmentId(departmentId);
                  ExaminationRoom room = new ExaminationRoom();
                  room.setId(roomId);
                  newQueue.setRoom(room);
                  newQueue.setQueueDate(today);
                  newQueue.setLastIssuedSequence(0);
                  newQueue.setIsOpen(Boolean.TRUE);
                  return queueRepository.save(newQueue);
                });

    return queueMapper.toDailyQueueResponse(queue);
  }

  @Transactional
  public QueueDTO.TicketResponse issueTicket(QueueDTO.IssueTicketRequest req) {
    Reservation res =
        reservationRepository
            .findById(req.reservationId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Reservation not found: " + req.reservationId()));

    if (!Boolean.TRUE.equals(res.getIsPaid())
        && !Boolean.TRUE.equals(res.getStatus().getAllowsQueueing())) {
      throw new InvalidOperationException(
          "Cannot issue queue ticket: Reservation must be PAID at billing service before entering the queue");
    }

    if (ticketRepository.findByReservationId(res.getId()).isPresent()) {
      throw new InvalidOperationException(
          "Queue ticket has already been issued for this reservation");
    }

    if (res.getRoom() == null) {
      throw new InvalidOperationException(
          "Reservation has no assigned examination room; cannot route to queue");
    }

    LocalDate today = LocalDate.now();
    DailyQueue queue =
        queueRepository
            .findByDepartmentIdAndRoomIdAndQueueDate(
                res.getDepartmentId(), res.getRoom().getId(), today)
            .orElseGet(
                () -> {
                  DailyQueue newQueue = new DailyQueue();
                  newQueue.setDepartmentId(res.getDepartmentId());
                  newQueue.setRoom(res.getRoom());
                  newQueue.setQueueDate(today);
                  newQueue.setLastIssuedSequence(0);
                  newQueue.setIsOpen(Boolean.TRUE);
                  return queueRepository.save(newQueue);
                });

    int nextSeq = queue.getLastIssuedSequence() + 1;
    queue.setLastIssuedSequence(nextSeq);
    queueRepository.save(queue);

    QueuePriorityLevel priority;
    if (req.priorityId() != null) {
      priority =
          priorityRepository
              .findById(req.priorityId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Priority not found: " + req.priorityId()));
    } else {
      priority =
          priorityRepository
              .findByCode("NORMAL")
              .orElseGet(
                  () -> {
                    List<QueuePriorityLevel> all =
                        priorityRepository.findByIsActiveTrueOrderByWeightDesc();
                    return all.isEmpty() ? null : all.get(all.size() - 1);
                  });
    }

    String prefix =
        res.getCategory().getCode().substring(0, Math.min(4, res.getCategory().getCode().length()));
    String ticketCode = String.format("%s-%03d", prefix, nextSeq);

    QueueTicket ticket = new QueueTicket();
    ticket.setQueue(queue);
    ticket.setReservation(res);
    ticket.setTicketCode(ticketCode);
    ticket.setSequenceNumber(nextSeq);
    ticket.setPriority(priority);
    ticket.setStatus("WAITING");

    // Update reservation status to CHECKED_IN
    statusRepository
        .findByCode("CHECKED_IN")
        .ifPresent(
            checkedInStatus -> {
              res.setStatus(checkedInStatus);
              reservationRepository.save(res);
            });

    return queueMapper.toTicketResponse(ticketRepository.save(ticket));
  }

  @Transactional
  public QueueDTO.TicketResponse callNextTicket(UUID queueId) {
    DailyQueue queue =
        queueRepository
            .findById(queueId)
            .orElseThrow(() -> new ResourceNotFoundException("Queue not found: " + queueId));

    List<QueueTicket> waitingTickets = ticketRepository.findNextWaitingTickets(queueId);
    if (waitingTickets.isEmpty()) {
      throw new InvalidOperationException("No waiting tickets in queue");
    }

    QueueTicket nextTicket = waitingTickets.get(0);
    nextTicket.setStatus("CALLED");
    nextTicket.setCalledAt(new Date());

    queue.setCurrentTicketNumber(nextTicket.getTicketCode());
    queueRepository.save(queue);

    // Update reservation status to CALLED
    statusRepository
        .findByCode("CALLED")
        .ifPresent(
            calledStatus -> {
              nextTicket.getReservation().setStatus(calledStatus);
              reservationRepository.save(nextTicket.getReservation());
            });

    return queueMapper.toTicketResponse(ticketRepository.save(nextTicket));
  }

  @Transactional
  public QueueDTO.TicketResponse updateTicketStatus(UUID ticketId, String newStatus) {
    QueueTicket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

    ticket.setStatus(newStatus.toUpperCase());
    Date now = new Date();

    if ("SERVING".equalsIgnoreCase(newStatus)) {
      ticket.setServiceStartedAt(now);
      statusRepository
          .findByCode("IN_SERVICE")
          .ifPresent(
              s -> {
                ticket.getReservation().setStatus(s);
                reservationRepository.save(ticket.getReservation());
              });
    } else if ("COMPLETED".equalsIgnoreCase(newStatus)) {
      ticket.setCompletedAt(now);
      statusRepository
          .findByCode("COMPLETED")
          .ifPresent(
              s -> {
                ticket.getReservation().setStatus(s);
                reservationRepository.save(ticket.getReservation());
              });
    }

    return queueMapper.toTicketResponse(ticketRepository.save(ticket));
  }

  @Transactional(readOnly = true)
  public List<QueueDTO.TicketResponse> getQueueTickets(UUID queueId) {
    return queueMapper.toTicketResponseList(ticketRepository.findAllQueueTickets(queueId));
  }

  @Transactional(readOnly = true)
  public PageResponse<QueueDTO.TicketResponse> searchTickets(
      LocalDate date, String query, Pageable pageable) {
    return PageResponse.from(
        ticketRepository
            .findAll(QueueTicketSpecifications.search(date, query), pageable)
            .map(queueMapper::toTicketResponse));
  }
}
