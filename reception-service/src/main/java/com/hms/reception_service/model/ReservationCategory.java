package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "reservation_categories")
@EntityListeners(AuditingEntityListener.class)
public class ReservationCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "code", unique = true, nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "requires_physician", nullable = false)
  private Boolean requiresPhysician = Boolean.TRUE;

  @Column(name = "requires_prescription", nullable = false)
  private Boolean requiresPrescription = Boolean.FALSE;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}
