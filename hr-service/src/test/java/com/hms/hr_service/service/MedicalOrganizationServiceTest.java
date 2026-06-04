package com.hms.hr_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationRequestDTO;
import com.hms.hr_service.dto.MedicalOrganization.CreateMedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.MedicalOrganizationResponseDTO;
import com.hms.hr_service.dto.MedicalOrganization.UpdateMedicalOrganizationRequestDTO;
import com.hms.hr_service.enums.UserRole;
import com.hms.hr_service.exception.MedicalOrganizationNotFoundException;
import com.hms.hr_service.mapper.MedicalOrganizationMapper;
import com.hms.hr_service.messaging.AnalyticsEventPublisher;
import com.hms.hr_service.model.MedicalOrganization;
import com.hms.hr_service.repository.MedicalOrganizationRepository;
import com.hms.hr_service.security.AuthenticatedPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MedicalOrganizationServiceTest {

  @Mock private MedicalOrganizationRepository medicalOrganizationRepository;
  @Mock private MedicalOrganizationMapper medicalOrganizationMapper;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;

  @InjectMocks private MedicalOrganizationService medicalOrganizationService;

  private UUID orgId;
  private UUID adminId;
  private AuthenticatedPrincipal principal;
  private MedicalOrganization organization;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    adminId = UUID.randomUUID();
    principal = new AuthenticatedPrincipal(adminId, UserRole.ADMIN);

    organization = new MedicalOrganization();
    organization.setId(orgId);
    organization.setAdminId(adminId);
    organization.setName("City Hospital");
    organization.setIdentifier("CITY-HOSP-01");
  }

  @Test
  void create_setsAdminIdAndReturnsDTO() {
    CreateMedicalOrganizationRequestDTO request =
        new CreateMedicalOrganizationRequestDTO(
            "City Hospital", "City Hospital LLC", "CITY-HOSP-01");
    CreateMedicalOrganizationResponseDTO responseDTO =
        CreateMedicalOrganizationResponseDTO.builder()
            .id(orgId.toString())
            .name("City Hospital")
            .legalName("City Hospital LLC")
            .identifier("CITY-HOSP-01")
            .build();

    given(
            medicalOrganizationMapper.createMedicalOrganizationRequestDTOToMedicalOrganization(
                request, adminId))
        .willReturn(organization);
    given(medicalOrganizationRepository.save(organization)).willReturn(organization);
    given(
            medicalOrganizationMapper.medicalOrganizationToCreateMedicalOrganizationResponseDTO(
                organization))
        .willReturn(responseDTO);

    CreateMedicalOrganizationResponseDTO result =
        medicalOrganizationService.create(request, principal);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("City Hospital");
    assertThat(organization.getAdminId()).isEqualTo(adminId);
    verify(medicalOrganizationRepository).save(organization);
  }

  @Test
  void update_existingOrg_updatesFields() {
    UpdateMedicalOrganizationRequestDTO updateDTO =
        new UpdateMedicalOrganizationRequestDTO("New Name", "New Legal Name", "CITY-HOSP-01");

    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.of(organization));
    given(medicalOrganizationRepository.save(organization)).willReturn(organization);

    medicalOrganizationService.update(orgId.toString(), updateDTO, principal);

    verify(medicalOrganizationMapper).updateMedicalOrganizationFromDTO(updateDTO, organization);
    verify(medicalOrganizationRepository).save(organization);
  }

  @Test
  void update_notFound_throwsMedicalOrganizationNotFoundException() {
    UpdateMedicalOrganizationRequestDTO updateDTO =
        new UpdateMedicalOrganizationRequestDTO("New Name", "New Legal Name", "CITY-HOSP-01");

    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.empty());

    assertThatThrownBy(
            () -> medicalOrganizationService.update(orgId.toString(), updateDTO, principal))
        .isInstanceOf(MedicalOrganizationNotFoundException.class);
  }

  @Test
  void delete_existingOrg_deletesEntity() {
    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.of(organization));

    medicalOrganizationService.delete(orgId.toString(), principal);

    verify(medicalOrganizationRepository).delete(organization);
  }

  @Test
  void delete_notFound_throwsMedicalOrganizationNotFoundException() {
    given(medicalOrganizationRepository.findByIdAndAdminId(orgId, adminId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> medicalOrganizationService.delete(orgId.toString(), principal))
        .isInstanceOf(MedicalOrganizationNotFoundException.class);
  }

  @Test
  void getById_existingOrg_returnsDTO() {
    MedicalOrganizationResponseDTO responseDTO =
        MedicalOrganizationResponseDTO.builder()
            .id(orgId.toString())
            .name("City Hospital")
            .legalName("City Hospital LLC")
            .identifier("CITY-HOSP-01")
            .build();

    given(medicalOrganizationRepository.findById(orgId)).willReturn(Optional.of(organization));
    given(
            medicalOrganizationMapper.medicalOrganizationToMedicalOrganizationResponseDTO(
                organization))
        .willReturn(responseDTO);

    MedicalOrganizationResponseDTO result = medicalOrganizationService.getById(orgId.toString());

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(orgId.toString());
  }

  @Test
  void getById_notFound_throwsMedicalOrganizationNotFoundException() {
    given(medicalOrganizationRepository.findById(orgId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> medicalOrganizationService.getById(orgId.toString()))
        .isInstanceOf(MedicalOrganizationNotFoundException.class);
  }
}
