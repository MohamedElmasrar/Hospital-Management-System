package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "physician")
public class Physician {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false, unique = true)
  private String licenseNumber;
}
