package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "employee_credentials")
public class EmployeeCredential {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false)
  private String credentialType;

  @Column(nullable = false)
  private String credentialNumber;

  @Column(nullable = false)
  private LocalDate expiresOn;
}
