package com.hms.billing_service.model;

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
@Table(name = "cash_desks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashDesk {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "desk_code", length = 30, unique = true, nullable = false)
  private String deskCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;

  @LastModifiedDate
  @Column(name = "date_modified", nullable = false)
  private Date dateModified;
}

