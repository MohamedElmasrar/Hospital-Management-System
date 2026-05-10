package com.hms.hr_service.service;

import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.GetMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.UpdateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.PageResponse;
import com.hms.hr_service.exception.MedicalOrganizationNotFoundException;
import com.hms.hr_service.mapper.MedicalOrganizationMapper;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.MedicalOrganizationRepository;
import com.hms.hr_service.specification.MedicalOrganizationSpecification;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MedicalOrganizationService {

  private final MedicalOrganizationRepository medicalOrganizationRepository;
  private final MedicalOrganizationMapper medicalOrganizationMapper;

  public PageResponse<MedicalOrganizationResponseDTO> search(
      GetMedicalOrganizationRequestDTO filter, Pageable pageable) {
    Specification<MedicalOrganization> spec =
        Specification.where(MedicalOrganizationSpecification.hasSearch(filter.search()))
            .and(MedicalOrganizationSpecification.hasName(filter.name()));

    Page<MedicalOrganization> page = medicalOrganizationRepository.findAll(spec, pageable);

    return PageResponse.from(
        page.map(medicalOrganizationMapper::medicalOrganizationToMedicalOrganizationResponseDTO));
  }

  public CreateMedicalOrganizationResponseDTO create(
      CreateMedicalOrganizationRequestDTO medicalOrganizationRequestDTO) {
    return medicalOrganizationMapper.medicalOrganizationToCreateMedicalOrganizationResponseDTO(
        medicalOrganizationRepository.save(
            medicalOrganizationMapper.createMedicalOrganizationRequestDTOToMedicalOrganization(
                medicalOrganizationRequestDTO)));
  }

  public void update(
      String id, UpdateMedicalOrganizationRequestDTO updateMedicalOrganizationRequestDTO) {
    MedicalOrganization medicalOrganization =
        medicalOrganizationRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> new MedicalOrganizationNotFoundException());
    medicalOrganizationMapper.updateMedicalOrganizationFromDTO(
        updateMedicalOrganizationRequestDTO, medicalOrganization);

    medicalOrganizationRepository.save(medicalOrganization);
  }

  public void delete(String id) {
    medicalOrganizationRepository.deleteById(UUID.fromString(id));
  }

  public MedicalOrganizationResponseDTO getById(String id) {
    return medicalOrganizationMapper.medicalOrganizationToMedicalOrganizationResponseDTO(
        medicalOrganizationRepository.findById(UUID.fromString(id)).get());
  }
}
