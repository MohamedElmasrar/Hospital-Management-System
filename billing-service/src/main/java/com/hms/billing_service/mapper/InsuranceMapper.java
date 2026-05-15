package com.hms.billing_service.mapper;

import com.hms.billing_service.dto.insurance.CreateInsuranceProviderRequestDTO;
import com.hms.billing_service.dto.insurance.InsuranceClaimResponseDTO;
import com.hms.billing_service.dto.insurance.InsurancePolicyResponseDTO;
import com.hms.billing_service.dto.insurance.InsuranceProviderResponseDTO;
import com.hms.billing_service.model.InsuranceClaim;
import com.hms.billing_service.model.InsuranceProvider;
import com.hms.billing_service.model.PatientInsurancePolicy;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InsuranceMapper {

  InsuranceProviderResponseDTO insuranceProviderToResponseDTO(InsuranceProvider provider);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "dateModified", ignore = true)
  InsuranceProvider createProviderDTOToEntity(CreateInsuranceProviderRequestDTO dto);

  List<InsuranceProviderResponseDTO> toInsuranceProviderResponseDTOList(
      List<InsuranceProvider> providers);

  @Mapping(source = "insuranceProvider.id", target = "insuranceProviderId")
  @Mapping(source = "insuranceProvider.name", target = "insuranceProviderName")
  @Mapping(source = "insuranceProvider.code", target = "insuranceProviderCode")
  InsurancePolicyResponseDTO policyToResponseDTO(PatientInsurancePolicy policy);

  List<InsurancePolicyResponseDTO> toPolicyResponseDTOList(List<PatientInsurancePolicy> policies);

  @Mapping(source = "invoice.id", target = "invoiceId")
  @Mapping(source = "invoice.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "policy.id", target = "policyId")
  @Mapping(source = "policy.policyNumber", target = "policyNumber")
  @Mapping(source = "policy.insuranceProvider.name", target = "insuranceProviderName")
  InsuranceClaimResponseDTO claimToResponseDTO(InsuranceClaim claim);

  List<InsuranceClaimResponseDTO> toClaimResponseDTOList(List<InsuranceClaim> claims);
}

