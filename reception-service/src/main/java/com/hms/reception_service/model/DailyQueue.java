package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "daily_queues",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_queue_dept_room_date",
          columnNames = {"department_id", "room_id", "queue_date"})
    })
public class DailyQueue {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ExaminationRoom room;

  @Column(name = "queue_date", nullable = false)
  private LocalDate queueDate = LocalDate.now();

  @Column(name = "current_ticket_number", length = 20)
  private String currentTicketNumber;

  @Column(name = "last_issued_sequence", nullable = false)
  private Integer lastIssuedSequence = 0;

  @Column(name = "is_open", nullable = false)
  private Boolean isOpen = Boolean.TRUE;
}
