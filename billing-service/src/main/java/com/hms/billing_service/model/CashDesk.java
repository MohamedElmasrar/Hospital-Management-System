package com.hms.billing_service.model;

import com.hms.billing_service.enums.BillingSectionType;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "cash_desks")
@EntityListeners(AuditingEntityListener.class)
@Data
public class CashDesk {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "desk_code", length = 30, unique = true, nullable = false)
  private String deskCode;

  @Column(name = "department_id", nullable = false)
  private UUID departmentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "section", length = 30, nullable = false)
  private BillingSectionType section;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}
