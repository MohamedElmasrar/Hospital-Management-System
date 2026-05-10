package com.hms.hr_service.model;

import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.enums.GenderType;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@Entity
@Table(name = "employee")
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String identifier;

  @Column(nullable = false)
  private String givenName;

  private String additionalName;

  @Column(nullable = false)
  private String familyName;

  private String honorificPrefix;

  private Date birthDate;

  private GenderType gender;

  private EmploymentStatus employmentStatus;

  @Column(nullable = false)
  @CreatedDate
  private Date dateCreated;

  @Column(nullable = false)
  @LastModifiedDate
  private Date dateModified;
}
