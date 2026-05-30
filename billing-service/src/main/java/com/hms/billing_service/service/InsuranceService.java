package com.hms.billing_service.service;

import com.hms.billing_service.dto.PageResponse;
import com.hms.billing_service.dto.insurance.*;
import com.hms.billing_service.enums.ClaimStatus;
import com.hms.billing_service.enums.InvoiceStatus;
import com.hms.billing_service.exception.*;
import com.hms.billing_service.mapper.InsuranceMapper;
import com.hms.billing_service.messaging.AnalyticsEventPublisher;
import com.hms.billing_service.model.InsuranceClaim;
import com.hms.billing_service.model.InsuranceProvider;
import com.hms.billing_service.model.Invoice;
import com.hms.billing_service.model.PatientInsurancePolicy;
import com.hms.billing_service.repository.InsuranceClaimRepository;
import com.hms.billing_service.repository.InsuranceProviderRepository;
import com.hms.billing_service.repository.InvoiceRepository;
import com.hms.billing_service.repository.PatientInsurancePolicyRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InsuranceService {

  private final InsuranceProviderRepository providerRepository;
  private final PatientInsurancePolicyRepository policyRepository;
  private final InsuranceClaimRepository claimRepository;
  private final InvoiceRepository invoiceRepository;
  private final InvoiceService invoiceService;
  private final InsuranceMapper insuranceMapper;
  private final AnalyticsEventPublisher analyticsEventPublisher;

  // --- Provider Operations ---

  @Transactional
  public InsuranceProviderResponseDTO createProvider(CreateInsuranceProviderRequestDTO request) {
    if (providerRepository.existsByCode(request.code())) {
      throw new ResourceAlreadyExistsException(
          "Insurance provider already exists with code: " + request.code());
    }
    if (providerRepository.existsByName(request.name())) {
      throw new ResourceAlreadyExistsException(
          "Insurance provider already exists with name: " + request.name());
    }

    InsuranceProvider provider = insuranceMapper.createProviderDTOToEntity(request);
    InsuranceProvider saved = providerRepository.save(provider);
    return insuranceMapper.insuranceProviderToResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public InsuranceProviderResponseDTO getProviderById(UUID id) {
    InsuranceProvider provider =
        providerRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new InsuranceProviderNotFoundException(
                        "Insurance provider not found with ID: " + id));
    return insuranceMapper.insuranceProviderToResponseDTO(provider);
  }

  @Transactional(readOnly = true)
  public List<InsuranceProviderResponseDTO> getAllProviders() {
    return insuranceMapper.toInsuranceProviderResponseDTOList(providerRepository.findAll());
  }

  // --- Policy Operations ---

  @Transactional
  public InsurancePolicyResponseDTO createPolicy(CreateInsurancePolicyRequestDTO request) {
    if (policyRepository.existsByInsuranceProviderIdAndPolicyNumber(
        request.insuranceProviderId(), request.policyNumber())) {
      throw new ResourceAlreadyExistsException(
          "Policy number "
              + request.policyNumber()
              + " already exists for provider "
              + request.insuranceProviderId());
    }

    InsuranceProvider provider =
        providerRepository
            .findById(request.insuranceProviderId())
            .orElseThrow(
                () ->
                    new InsuranceProviderNotFoundException(
                        "Insurance provider not found with ID: " + request.insuranceProviderId()));

    PatientInsurancePolicy policy = new PatientInsurancePolicy();
    policy.setPolicyNumber(request.policyNumber());
    policy.setInsuranceProvider(provider);
    policy.setPatientId(request.patientId());
    policy.setCoverageRate(request.coverageRate());
    policy.setIsActive(true);

    PatientInsurancePolicy saved = policyRepository.save(policy);
    analyticsEventPublisher.publish("INSURANCE_POLICY_CREATED", "INSURANCE_POLICY", saved.getId());
    return insuranceMapper.policyToResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public InsurancePolicyResponseDTO getPolicyById(UUID id) {
    PatientInsurancePolicy policy =
        policyRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new InsurancePolicyNotFoundException(
                        "Insurance policy not found with ID: " + id));
    return insuranceMapper.policyToResponseDTO(policy);
  }

  @Transactional(readOnly = true)
  public List<InsurancePolicyResponseDTO> getPoliciesByPatient(UUID patientId) {
    return insuranceMapper.toPolicyResponseDTOList(policyRepository.findByPatientId(patientId));
  }

  // --- Claim Operations ---

  @Transactional
  public InsuranceClaimResponseDTO submitClaim(SubmitClaimRequestDTO request) {
    if (claimRepository.existsByInvoiceId(request.invoiceId())) {
      throw new ResourceAlreadyExistsException(
          "Claim has already been submitted for invoice: " + request.invoiceId());
    }

    Invoice invoice =
        invoiceRepository
            .findById(request.invoiceId())
            .orElseThrow(
                () ->
                    new InvoiceNotFoundException(
                        "Invoice not found with ID: " + request.invoiceId()));

    PatientInsurancePolicy policy =
        policyRepository
            .findById(request.policyId())
            .orElseThrow(
                () ->
                    new InsurancePolicyNotFoundException(
                        "Insurance policy not found with ID: " + request.policyId()));

    if (!policy.getPatientId().equals(invoice.getPatientId())) {
      throw new InvalidOperationException("Policy does not belong to the invoice patient");
    }

    InvoiceService.InvoiceCalculations calc = invoiceService.calculate(invoice);
    if (request.claimedAmount().compareTo(calc.totalAmount()) > 0) {
      throw new InvalidOperationException(
          "Claimed amount cannot exceed invoice total amount: " + calc.totalAmount());
    }

    InsuranceClaim claim = new InsuranceClaim();
    claim.setInvoice(invoice);
    claim.setPolicy(policy);
    claim.setClaimedAmount(request.claimedAmount());
    claim.setStatus(ClaimStatus.SUBMITTED);
    claim.setSubmittedAt(new Date());

    InsuranceClaim saved = claimRepository.save(claim);
    analyticsEventPublisher.publish("INSURANCE_CLAIM_SUBMITTED", "INSURANCE_CLAIM", saved.getId());
    return insuranceMapper.claimToResponseDTO(saved);
  }

  @Transactional
  public InsuranceClaimResponseDTO updateClaimStatus(
      UUID claimId, UpdateClaimStatusRequestDTO request) {
    InsuranceClaim claim =
        claimRepository
            .findById(claimId)
            .orElseThrow(
                () ->
                    new InsuranceClaimNotFoundException(
                        "Insurance claim not found with ID: " + claimId));

    ClaimStatus oldStatus = claim.getStatus();
    claim.setStatus(request.status());
    if (request.status() == ClaimStatus.APPROVED || request.status() == ClaimStatus.SETTLED) {
      BigDecimal approved =
          request.approvedAmount() != null ? request.approvedAmount() : claim.getClaimedAmount();
      claim.setApprovedAmount(approved);
      if (request.status() == ClaimStatus.SETTLED) {
        claim.setSettledAt(new Date());
      }
    }

    InsuranceClaim updated = claimRepository.save(claim);
    if (oldStatus != updated.getStatus()) {
      analyticsEventPublisher.publish(
          "INSURANCE_CLAIM_" + updated.getStatus().name(), "INSURANCE_CLAIM", updated.getId());
    }

    // Re-check invoice balance
    Invoice invoice = claim.getInvoice();
    InvoiceService.InvoiceCalculations calc = invoiceService.calculate(invoice);
    if (calc.balanceDue().compareTo(BigDecimal.ZERO) <= 0
        && invoice.getStatus() != InvoiceStatus.VOID) {
      boolean becamePaid = invoice.getStatus() != InvoiceStatus.PAID;
      invoice.setStatus(InvoiceStatus.PAID);
      invoiceRepository.save(invoice);
      if (becamePaid) {
        analyticsEventPublisher.publish("INVOICE_PAID", "INVOICE", invoice.getId());
      }
    }

    return insuranceMapper.claimToResponseDTO(updated);
  }

  @Transactional(readOnly = true)
  public InsuranceClaimResponseDTO getClaimById(UUID id) {
    InsuranceClaim claim =
        claimRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new InsuranceClaimNotFoundException(
                        "Insurance claim not found with ID: " + id));
    return insuranceMapper.claimToResponseDTO(claim);
  }

  @Transactional(readOnly = true)
  public PageResponse<InsuranceClaimResponseDTO> getClaimsByStatus(
      ClaimStatus status, Pageable pageable) {
    return PageResponse.from(
        claimRepository.findByStatus(status, pageable).map(insuranceMapper::claimToResponseDTO));
  }
}
