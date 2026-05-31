package com.hms.reception_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.exception.GlobalExceptionHandler;
import com.hms.reception_service.security.SecurityConfig;
import com.hms.reception_service.service.PatientService;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PatientController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PatientControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private PatientService patientService;

  @BeforeEach
  void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void createPatient_whenEmployee_shouldReturnCreated() throws Exception {
    PatientDTO.CreateRequest req =
        new PatientDTO.CreateRequest(
            "CIN12345",
            "John",
            "Doe",
            LocalDate.of(1990, 5, 20),
            "MALE",
            "O_POSITIVE",
            "+212600000000",
            null,
            null,
            "None",
            "None");

    PatientDTO.Response res =
        new PatientDTO.Response(
            UUID.randomUUID(),
            "MRN-20261017-001",
            "CIN12345",
            "John",
            "Doe",
            LocalDate.of(1990, 5, 20),
            "MALE",
            "O_POSITIVE",
            "+212600000000",
            null,
            null,
            "None",
            "None",
            true,
            new Date(),
            new Date());

    when(patientService.createPatient(any())).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/patients")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.mrn").value("MRN-20261017-001"))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void getPatientById_whenDoctor_shouldReturnPatient() throws Exception {
    UUID id = UUID.randomUUID();
    PatientDTO.Response res =
        new PatientDTO.Response(
            id,
            "MRN-20261017-001",
            "CIN12345",
            "John",
            "Doe",
            LocalDate.of(1990, 5, 20),
            "MALE",
            "O_POSITIVE",
            "+212600000000",
            null,
            null,
            "None",
            "None",
            true,
            new Date(),
            new Date());

    when(patientService.getPatientById(id)).thenReturn(res);

    mockMvc
        .perform(
            get("/api/reception/patients/{id}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mrn").value("MRN-20261017-001"));
  }
}
