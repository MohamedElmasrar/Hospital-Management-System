package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "payroll_settings")
public class PayrollSettings {
  @Id private UUID organizationId;

  @Column(nullable = false, length = 3)
  private String currency = "MAD";

  @Column(nullable = false)
  private int payDay = 28;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal overtimeHourlyRate = BigDecimal.ZERO;
}
