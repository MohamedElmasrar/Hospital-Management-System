package com.hms.reception_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.exception.GlobalExceptionHandler;
import com.hms.reception_service.security.SecurityConfig;
import com.hms.reception_service.service.ReservationService;
import java.time.LocalDate;
import java.time.LocalTime;
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

@WebMvcTest(ReservationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ReservationControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ReservationService reservationService;

  @BeforeEach
  void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void createConsultation_whenEmployee_shouldReturnCreated() throws Exception {
    UUID patientId = UUID.randomUUID();
    UUID deptId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    ReservationDTO.CreateConsultationRequest req =
        new ReservationDTO.CreateConsultationRequest(
            patientId,
            deptId,
            practitionerId,
            null,
            LocalDate.of(2026, 10, 20),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            "Fever",
            "Notes");

    ReservationDTO.Response res =
        new ReservationDTO.Response(
            UUID.randomUUID(),
            "CONS-20261020-001",
            patientId,
            "John Doe",
            "+212600000000",
            UUID.randomUUID(),
            "Consultation",
            "CONSULTATION",
            deptId,
            practitionerId,
            null,
            null,
            null,
            LocalDate.of(2026, 10, 20),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            UUID.randomUUID(),
            "PENDING_PAYMENT",
            "Pending Payment",
            "#F59E0B",
            false,
            null,
            "Fever",
            "Notes",
            UUID.randomUUID(),
            null,
            new Date(),
            new Date());

    when(reservationService.createConsultationReservation(any(), any())).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/reservations/consultation")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reservationNumber").value("CONS-20261020-001"))
        .andExpect(jsonPath("$.isPaid").value(false));
  }

  @Test
  void markAsPaid_whenEmployee_shouldReturnOk() throws Exception {
    UUID resId = UUID.randomUUID();
    UUID invoiceId = UUID.randomUUID();

    ReservationDTO.Response res =
        new ReservationDTO.Response(
            resId,
            "CONS-20261020-001",
            UUID.randomUUID(),
            "John Doe",
            "+212600000000",
            UUID.randomUUID(),
            "Consultation",
            "CONSULTATION",
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            null,
            LocalDate.of(2026, 10, 20),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            UUID.randomUUID(),
            "PAID",
            "Paid / Ready for Queue",
            "#3B82F6",
            true,
            invoiceId,
            "Fever",
            "Notes",
            UUID.randomUUID(),
            null,
            new Date(),
            new Date());

    when(reservationService.markAsPaid(resId, invoiceId)).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/reservations/{id}/pay", resId)
                .param("invoiceId", invoiceId.toString())
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isPaid").value(true))
        .andExpect(jsonPath("$.statusCode").value("PAID"));
  }
}
