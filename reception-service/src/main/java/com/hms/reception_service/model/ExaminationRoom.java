package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "examination_rooms")
@EntityListeners(AuditingEntityListener.class)
public class ExaminationRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "room_code", unique = true, nullable = false, length = 30)
  private String roomCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modality_id")
  private DiagnosticModality modality;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}
