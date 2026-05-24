package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "beds")
public class Bed {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "bed_code", unique = true, nullable = false, length = 30)
  private String bedCode;

  @Column(name = "ward", nullable = false, length = 100)
  private String ward;

  @Column(name = "occupied", nullable = false)
  private boolean occupied;

  @Column(name = "patient_id")
  private UUID patientId;
}
