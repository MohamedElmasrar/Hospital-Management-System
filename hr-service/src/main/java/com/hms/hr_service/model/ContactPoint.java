package com.hms.hr_service.model;

import com.hms.hr_service.enums.ContactType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "contact_point")
public class ContactPoint {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(
      name = "employee_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_contact_point_employee"))
  private Employee employee;

  @Column(nullable = false)
  private ContactType contactType;

  private String telephone;

  private String email;

  @Column(nullable = false)
  private boolean isPreferred = false;
}
