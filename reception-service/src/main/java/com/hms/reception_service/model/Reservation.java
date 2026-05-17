package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(
    name = "reservations",
    indexes = {
      @Index(name = "idx_reservations_patient", columnList = "patient_id, reservation_date"),
      @Index(name = "idx_reservations_status", columnList = "status_id, is_paid"),
      @Index(
          name = "idx_reservations_practitioner",
          columnList = "practitioner_id, reservation_date")
    })
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "reservation_number", unique = true, nullable = false, length = 30)
  private String reservationNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ReservationCategory category;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @Column(name = "practitioner_id")
  private UUID practitionerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prescription_id")
  private DiagnosticPrescription prescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private ExaminationRoom room;

  @Column(name = "reservation_date", nullable = false)
  private LocalDate reservationDate;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "status_id", nullable = false)
  private ReservationStatus status;

  @Column(name = "is_paid", nullable = false)
  private Boolean isPaid = Boolean.FALSE;

  @Column(name = "billing_invoice_id")
  private UUID billingInvoiceId;

  @Column(name = "reason_for_visit")
  private String reasonForVisit;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "receptionist_staff_id", nullable = false)
  private UUID receptionistStaffId;

  @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private DiagnosticReservationDetail diagnosticDetail;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Date createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;
}
