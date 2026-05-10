package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(
    name = "physician_specialty",
    indexes = {
      @Index(
          name = "idx_physician_id_medical_specialty_id",
          columnList = "physician_id, medical_specialty_id")
    })
public class PhysicianSpecialty {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "physician_id", nullable = false)
  private Physician physician;

  @ManyToOne
  @JoinColumn(name = "medical_specialty_id", nullable = false)
  private MedicalSpecialty medicalSpecialty;

  private boolean isPrimary = false;
}
