package com.hms.hr_service.model;

import com.hms.hr_service.enums.PayrollStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "payroll_records",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"organization_id", "employee_id", "period"}))
public class PayrollRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false)
  private LocalDate period;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private LocalDate payDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PayrollStatus status = PayrollStatus.DRAFT;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal baseSalary;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal allowance;

  @Column(nullable = false, precision = 6, scale = 2)
  private BigDecimal overtimeHours;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal overtimeHourlyRate;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal overtimePay;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal extraDeductions;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal deductions;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal grossPay;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal netPay;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  private Instant approvedAt;
  private UUID approvedBy;
  private Instant paidAt;
  private UUID paidBy;
  @Version private Long version;
}
