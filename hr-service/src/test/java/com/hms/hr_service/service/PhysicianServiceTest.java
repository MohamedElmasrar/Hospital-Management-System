package com.hms.hr_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hms.hr_service.dto.Physician.CreateMedicalSpecialtyRequestDTO;
import com.hms.hr_service.dto.Physician.CreatePhysicianRequestDTO;
import com.hms.hr_service.dto.Physician.MedicalSpecialtyResponseDTO;
import com.hms.hr_service.dto.Physician.PhysicianResponseDTO;
import com.hms.hr_service.exception.EmployeeNotFoundException;
import com.hms.hr_service.exception.PhysicianNotFoundException;
import com.hms.hr_service.exception.ResourceAlreadyExistsException;
import com.hms.hr_service.mapper.MedicalSpecialtyMapper;
import com.hms.hr_service.mapper.PhysicianMapper;
import com.hms.hr_service.messaging.AnalyticsEventPublisher;
import com.hms.hr_service.model.Employee;
import com.hms.hr_service.model.MedicalSpecialty;
import com.hms.hr_service.model.Physician;
import com.hms.hr_service.repository.EmployeeRepository;
import com.hms.hr_service.repository.MedicalSpecialtyRepository;
import com.hms.hr_service.repository.PhysicianRepository;
import com.hms.hr_service.repository.PhysicianSpecialtyRepository;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhysicianServiceTest {

  @Mock private PhysicianRepository physicianRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private MedicalSpecialtyRepository medicalSpecialtyRepository;
  @Mock private PhysicianSpecialtyRepository physicianSpecialtyRepository;
  @Mock private PhysicianMapper physicianMapper;
  @Mock private MedicalSpecialtyMapper medicalSpecialtyMapper;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;

  @InjectMocks private PhysicianService physicianService;

  private UUID physicianId;
  private UUID employeeId;
  private Employee employee;
  private Physician physician;

  @BeforeEach
  void setUp() {
    physicianId = UUID.randomUUID();
    employeeId = UUID.randomUUID();

    employee = new Employee();
    employee.setId(employeeId);
    employee.setGivenName("Alice");
    employee.setFamilyName("Smith");
    employee.setIdentifier("DOC-001");

    physician = new Physician();
    physician.setId(physicianId);
    physician.setEmployee(employee);
    physician.setLicenseNumber("LIC-12345");
  }

  @Test
  void create_validRequest_createsPhysician() {
    CreatePhysicianRequestDTO request =
        new CreatePhysicianRequestDTO(employeeId, "LIC-12345", null, null);

    PhysicianResponseDTO responseDTO =
        new PhysicianResponseDTO(
            physicianId,
            employeeId,
            "DOC-001",
            "Alice Smith",
            "LIC-12345",
            Collections.emptyList());

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(physicianRepository.existsByEmployeeId(employeeId)).willReturn(false);
    given(physicianRepository.existsByLicenseNumber("LIC-12345")).willReturn(false);
    given(physicianMapper.toPhysician(request, employee)).willReturn(physician);
    given(physicianRepository.save(physician)).willReturn(physician);
    given(physicianMapper.toPhysicianResponseDTO(physician, Collections.emptyList()))
        .willReturn(responseDTO);

    PhysicianResponseDTO result = physicianService.create(request);

    assertThat(result).isNotNull();
    assertThat(result.licenseNumber()).isEqualTo("LIC-12345");
    verify(physicianRepository).save(any(Physician.class));
    verify(analyticsEventPublisher).publish("PHYSICIAN_CREATED", "PHYSICIAN", physicianId);
  }

  @Test
  void create_employeeNotFound_throwsEmployeeNotFoundException() {
    CreatePhysicianRequestDTO request =
        new CreatePhysicianRequestDTO(employeeId, "LIC-12345", null, null);

    given(employeeRepository.findById(employeeId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> physicianService.create(request))
        .isInstanceOf(EmployeeNotFoundException.class);
  }

  @Test
  void create_duplicateEmployee_throwsResourceAlreadyExistsException() {
    CreatePhysicianRequestDTO request =
        new CreatePhysicianRequestDTO(employeeId, "LIC-12345", null, null);

    given(employeeRepository.findById(employeeId)).willReturn(Optional.of(employee));
    given(physicianRepository.existsByEmployeeId(employeeId)).willReturn(true);

    assertThatThrownBy(() -> physicianService.create(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  void getById_existingPhysician_returnsPhysician() {
    PhysicianResponseDTO responseDTO =
        new PhysicianResponseDTO(
            physicianId,
            employeeId,
            "DOC-001",
            "Alice Smith",
            "LIC-12345",
            Collections.emptyList());

    given(physicianRepository.findById(physicianId)).willReturn(Optional.of(physician));
    given(physicianSpecialtyRepository.findByPhysicianId(physicianId))
        .willReturn(Collections.emptyList());
    given(physicianMapper.toPhysicianSpecialtyResponseDTOList(Collections.emptyList()))
        .willReturn(Collections.emptyList());
    given(physicianMapper.toPhysicianResponseDTO(physician, Collections.emptyList()))
        .willReturn(responseDTO);

    PhysicianResponseDTO result = physicianService.getById(physicianId);

    assertThat(result).isNotNull();
    assertThat(result.licenseNumber()).isEqualTo("LIC-12345");
  }

  @Test
  void getById_notFound_throwsPhysicianNotFoundException() {
    given(physicianRepository.findById(physicianId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> physicianService.getById(physicianId))
        .isInstanceOf(PhysicianNotFoundException.class);
  }

  @Test
  void createSpecialty_validRequest_createsSpecialty() {
    CreateMedicalSpecialtyRequestDTO request =
        new CreateMedicalSpecialtyRequestDTO("Neurology", "Brain and nervous system");
    MedicalSpecialty specialty = new MedicalSpecialty();
    specialty.setName("Neurology");

    MedicalSpecialtyResponseDTO responseDTO =
        new MedicalSpecialtyResponseDTO(UUID.randomUUID(), "Neurology", "Brain and nervous system");

    given(medicalSpecialtyRepository.existsByName("Neurology")).willReturn(false);
    given(medicalSpecialtyMapper.createMedicalSpecialtyRequestDTOToMedicalSpecialty(request))
        .willReturn(specialty);
    given(medicalSpecialtyRepository.save(specialty)).willReturn(specialty);
    given(medicalSpecialtyMapper.medicalSpecialtyToMedicalSpecialtyResponseDTO(specialty))
        .willReturn(responseDTO);

    MedicalSpecialtyResponseDTO result = physicianService.createSpecialty(request);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Neurology");
    verify(medicalSpecialtyRepository).save(specialty);
  }
}
