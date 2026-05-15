package com.hms.billing_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "service_tariff_versions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_tariff_catalog_item_effective_from",
          columnNames = {"catalog_item_id", "effective_from"})
    },
    indexes = {
      @Index(name = "idx_tariff_catalog_effective", columnList = "catalog_item_id, effective_to")
    })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTariffVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "catalog_item_id", nullable = false)
  private ServiceCatalog catalogItem;

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
  private BigDecimal taxRate = BigDecimal.ZERO;

  @Column(name = "effective_from", nullable = false)
  private Date effectiveFrom;

  @Column(name = "effective_to")
  private Date effectiveTo;

  @CreatedDate
  @Column(name = "date_created", nullable = false, updatable = false)
  private Date dateCreated;
}

