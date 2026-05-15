package com.hms.billing_service.repository;

import com.hms.billing_service.model.PatientInsurancePolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientInsurancePolicyRepository
    extends JpaRepository<PatientInsurancePolicy, UUID> {
  Optional<PatientInsurancePolicy> findByInsuranceProviderIdAndPolicyNumber(
      UUID insuranceProviderId, String policyNumber);

  boolean existsByInsuranceProviderIdAndPolicyNumber(UUID insuranceProviderId, String policyNumber);

  List<PatientInsurancePolicy> findByPatientId(UUID patientId);

  List<PatientInsurancePolicy> findByPatientIdAndIsActiveTrue(UUID patientId);
}

