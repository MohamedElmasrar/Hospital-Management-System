package com.hms.hr_service.service;

import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.GetMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.UpdateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.exception.MedicalOrganizationNotFoundException;
import com.hms.hr_service.mapper.MedicalOrganizationMapper;
import com.hms.hr_service.messaging.AnalyticsEventPublisher;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.MedicalOrganizationRepository;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import com.hms.hr_service.specification.MedicalOrganizationSpecification;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MedicalOrganizationService {

  private final MedicalOrganizationRepository medicalOrganizationRepository;
  private final MedicalOrganizationMapper medicalOrganizationMapper;
  private final AnalyticsEventPublisher analyticsEventPublisher;

  public PageResponse<MedicalOrganizationResponseDTO> search(
      GetMedicalOrganizationRequestDTO filter, Pageable pageable) {
    Specification<MedicalOrganization> spec =
        Specification.where(MedicalOrganizationSpecification.hasSearch(filter.search()))
            .and(MedicalOrganizationSpecification.hasName(filter.name()));

    Page<MedicalOrganization> page = medicalOrganizationRepository.findAll(spec, pageable);

    return PageResponse.from(
        page.map(medicalOrganizationMapper::medicalOrganizationToMedicalOrganizationResponseDTO));
  }

  @Transactional
  public CreateMedicalOrganizationResponseDTO create(
      CreateMedicalOrganizationRequestDTO medicalOrganizationRequestDTO,
      AuthenticatedPrincipal principal) {
    UUID adminId = (principal != null) ? principal.userId() : null;
    MedicalOrganization medicalOrganization =
        medicalOrganizationMapper.createMedicalOrganizationRequestDTOToMedicalOrganization(
            medicalOrganizationRequestDTO, adminId);
    MedicalOrganization saved = medicalOrganizationRepository.save(medicalOrganization);
    analyticsEventPublisher.publish(
        "MEDICAL_ORGANIZATION_CREATED", "MEDICAL_ORGANIZATION", saved.getId());
    return medicalOrganizationMapper.medicalOrganizationToCreateMedicalOrganizationResponseDTO(
        saved);
  }

  public void update(
      String id,
      UpdateMedicalOrganizationRequestDTO updateMedicalOrganizationRequestDTO,
      AuthenticatedPrincipal principal) {
    UUID orgId = UUID.fromString(id);
    MedicalOrganization medicalOrganization =
        (principal != null && principal.userId() != null)
            ? medicalOrganizationRepository
                .findByIdAndAdminId(orgId, principal.userId())
                .orElseThrow(MedicalOrganizationNotFoundException::new)
            : medicalOrganizationRepository
                .findById(orgId)
                .orElseThrow(MedicalOrganizationNotFoundException::new);

    medicalOrganizationMapper.updateMedicalOrganizationFromDTO(
        updateMedicalOrganizationRequestDTO, medicalOrganization);

    medicalOrganizationRepository.save(medicalOrganization);
  }

  public void delete(String id, AuthenticatedPrincipal principal) {
    UUID orgId = UUID.fromString(id);
    MedicalOrganization medOrg =
        (principal != null && principal.userId() != null)
            ? medicalOrganizationRepository
                .findByIdAndAdminId(orgId, principal.userId())
                .orElseThrow(MedicalOrganizationNotFoundException::new)
            : medicalOrganizationRepository
                .findById(orgId)
                .orElseThrow(MedicalOrganizationNotFoundException::new);
    medicalOrganizationRepository.delete(medOrg);
  }

  public MedicalOrganizationResponseDTO getById(String id) {
    return medicalOrganizationMapper.medicalOrganizationToMedicalOrganizationResponseDTO(
        medicalOrganizationRepository
            .findById(UUID.fromString(id))
            .orElseThrow(MedicalOrganizationNotFoundException::new));
  }
}
