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
    name = "diagnostic_prescriptions",
    indexes = {@Index(name = "idx_prescriptions_patient", columnList = "patient_id, is_fulfilled")})
@EntityListeners(AuditingEntityListener.class)
public class DiagnosticPrescription {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "prescription_code", unique = true, nullable = false, length = 30)
  private String prescriptionCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @Column(name = "referring_practitioner_id", nullable = false)
  private UUID referringPractitionerId;

  @Column(name = "originating_reservation_id")
  private UUID originatingReservationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modality_id", nullable = false)
  private DiagnosticModality modality;

  @Column(name = "body_part", nullable = false, length = 100)
  private String bodyPart;

  @Column(name = "contrast_required", nullable = false)
  private Boolean contrastRequired = Boolean.FALSE;

  @Column(name = "clinical_notes", columnDefinition = "TEXT")
  private String clinicalNotes;

  @Column(name = "is_fulfilled", nullable = false)
  private Boolean isFulfilled = Boolean.FALSE;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt;
}
