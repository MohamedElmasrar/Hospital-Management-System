package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(
    name = "queue_tickets",
    indexes = {@Index(name = "idx_tickets_queue", columnList = "queue_id, status")})
@EntityListeners(AuditingEntityListener.class)
public class QueueTicket {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "queue_id", nullable = false)
  private DailyQueue queue;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id", unique = true, nullable = false)
  private Reservation reservation;

  @Column(name = "ticket_code", nullable = false, length = 20)
  private String ticketCode;

  @Column(name = "sequence_number", nullable = false)
  private Integer sequenceNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "priority_id", nullable = false)
  private QueuePriorityLevel priority;

  @Column(name = "status", nullable = false, length = 30)
  private String status = "WAITING"; // WAITING, CALLED, SERVING, COMPLETED, SKIPPED

  @CreatedDate
  @Column(name = "issued_at", nullable = false, updatable = false)
  private Date issuedAt;

  @Column(name = "called_at")
  private Date calledAt;

  @Column(name = "service_started_at")
  private Date serviceStartedAt;

  @Column(name = "completed_at")
  private Date completedAt;
}
