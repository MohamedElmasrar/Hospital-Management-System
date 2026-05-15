package com.hms.billing_service.model;

import com.hms.billing_service.enums.BillingSectionType;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "departments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "name", length = 100, unique = true, nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "section", nullable = false)
  private BillingSectionType section;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}

