package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "service_schedules",
    indexes = {
      @Index(name = "idx_schedules_practitioner", columnList = "practitioner_id, day_of_week"),
      @Index(name = "idx_schedules_room", columnList = "room_id, day_of_week")
    })
public class ServiceSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @Column(name = "practitioner_id")
  private UUID practitionerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ExaminationRoom room;

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false, length = 15)
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "slot_duration_minutes", nullable = false)
  private Integer slotDurationMinutes = 30;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;
}
