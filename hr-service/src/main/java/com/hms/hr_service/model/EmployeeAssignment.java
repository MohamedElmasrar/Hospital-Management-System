package com.hms.hr_service.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Entity
@Data
@Table(
    name = "employee_assignment",
    indexes = {
      @Index(
          name = "idx_employee_id_department_id_start_date",
          columnList = "employee_id, department_id, start_date")
    })
public class EmployeeAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @ManyToOne
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;

  @Column(nullable = false)
  private String jobTitle;

  @Column(nullable = false, name = "start_date")
  private Date startDate;

  private Date endDate;
}
