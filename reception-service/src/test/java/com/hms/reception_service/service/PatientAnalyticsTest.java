package com.hms.reception_service.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.mapper.PatientMapper;
import com.hms.reception_service.messaging.AnalyticsEventPublisher;
import com.hms.reception_service.model.Patient;
import com.hms.reception_service.repository.PatientRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientAnalyticsTest {
  @Mock private PatientRepository patientRepository;
  @Mock private PatientMapper patientMapper;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;
  @InjectMocks private PatientService patientService;

  @Test
  void createdPatientEmitsOneEvent() {
    PatientDTO.CreateRequest request = request(null);
    Patient patient = new Patient();
    patient.setId(UUID.randomUUID());
    when(patientMapper.toEntity(request)).thenReturn(patient);
    when(patientRepository.save(patient)).thenReturn(patient);

    patientService.createPatient(request);

    verify(analyticsEventPublisher).publish("PATIENT_CREATED", "PATIENT", patient.getId());
  }

  @Test
  void duplicatePatientDoesNotEmitAnEvent() {
    PatientDTO.CreateRequest request = request("existing-id");
    when(patientRepository.existsByNationalId("existing-id")).thenReturn(true);

    assertThatThrownBy(() -> patientService.createPatient(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
    verifyNoInteractions(analyticsEventPublisher);
  }

  private PatientDTO.CreateRequest request(String nationalId) {
    return new PatientDTO.CreateRequest(
        nationalId,
        "Test",
        "Patient",
        LocalDate.of(1990, 1, 1),
        "OTHER",
        null,
        "0000000000",
        null,
        null,
        null,
        null);
  }
}
