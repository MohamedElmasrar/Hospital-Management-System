package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "diagnostic_reservation_details")
public class DiagnosticReservationDetail {

  @Id private UUID reservationId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Reservation reservation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modality_id", nullable = false)
  private DiagnosticModality modality;

  @Column(name = "body_part", nullable = false, length = 100)
  private String bodyPart;

  @Column(name = "contrast_used", nullable = false)
  private Boolean contrastUsed = Boolean.FALSE;

  @Column(name = "technician_id")
  private UUID technicianId;

  @Column(name = "safety_checklist_completed", nullable = false)
  private Boolean safetyChecklistCompleted = Boolean.FALSE;
}
