package com.hms.reception_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(
    name = "patients",
    indexes = {
      @Index(name = "idx_patients_search", columnList = "last_name, first_name, phone"),
      @Index(name = "idx_patients_national_id", columnList = "national_id")
    })
@EntityListeners(AuditingEntityListener.class)
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "mrn", unique = true, nullable = false, length = 30)
  private String mrn;

  @Column(name = "user_id", unique = true)
  private UUID userId;

  @Column(name = "national_id", unique = true, length = 50)
  private String nationalId;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @Column(name = "gender", nullable = false, length = 20)
  private String gender;

  @Column(name = "blood_group", length = 15)
  private String bloodGroup = "UNKNOWN";

  @Column(name = "phone", nullable = false, length = 30)
  private String phone;

  @Column(name = "emergency_contact_phone", length = 30)
  private String emergencyContactPhone;

  @Column(name = "emergency_contact_name", length = 150)
  private String emergencyContactName;

  @Column(name = "allergies", columnDefinition = "TEXT")
  private String allergies;

  @Column(name = "chronic_conditions", columnDefinition = "TEXT")
  private String chronicConditions;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = Boolean.TRUE;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}
