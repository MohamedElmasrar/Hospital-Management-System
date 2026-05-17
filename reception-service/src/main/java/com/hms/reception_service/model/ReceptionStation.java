package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "reception_stations")
public class ReceptionStation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "station_code", unique = true, nullable = false, length = 30)
  private String stationCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ReservationCategory category;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;
}
