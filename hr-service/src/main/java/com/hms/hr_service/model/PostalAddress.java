package com.hms.hr_service.model;

import com.hms.hr_service.enums.AddressType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "postal_address")
public class PostalAddress {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false)
  private AddressType addressType;

  @Column(nullable = false)
  private String streetAddress;

  @Column(nullable = false)
  private String addressLocality;

  @Column(nullable = false)
  private String addressRegion;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String addressCountry; // ISO 3166-1 alpha-2
}
