package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "attendance_records",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
public class AttendanceRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(name = "attendance_date", nullable = false)
  private LocalDate attendanceDate;

  private LocalDateTime checkedInAt;
  private LocalDateTime checkedOutAt;
}
