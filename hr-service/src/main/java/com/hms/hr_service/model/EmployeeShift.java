package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "employee_shifts")
public class EmployeeShift {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @Column(nullable = false)
  private LocalDate shiftDate;

  @Column(nullable = false)
  private LocalTime startsAt;

  @Column(nullable = false)
  private LocalTime endsAt;
}
