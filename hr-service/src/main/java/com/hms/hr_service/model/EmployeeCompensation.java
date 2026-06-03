package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "employee_compensations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "employee_id"}))
public class EmployeeCompensation {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal baseSalary = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal allowance = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal deductions = BigDecimal.ZERO;
}
