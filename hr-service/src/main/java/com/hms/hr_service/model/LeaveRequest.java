package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false)
  private LocalDate startsOn;

  @Column(nullable = false)
  private LocalDate endsOn;

  @Column(nullable = false)
  private String reason;

  @Column(nullable = false)
  private String status;

  private String decisionReason;
}
