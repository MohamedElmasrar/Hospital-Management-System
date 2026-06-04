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
        ReservationDTO.CreateConsultationRequest.builder()
            .patientId(patientId)
            .departmentId(deptId)
            .practitionerId(practitionerId)
            .reservationDate(LocalDate.of(2026, 10, 20))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 30))
            .reasonForVisit("Fever")
            .notes("Notes")
            .build();

    ReservationDTO.Response res =
        ReservationDTO.Response.builder()
            .id(UUID.randomUUID())
            .reservationNumber("CONS-20261020-001")
            .patientId(patientId)
            .patientName("John Doe")
            .patientPhone("+212600000000")
            .categoryId(UUID.randomUUID())
            .categoryName("Consultation")
            .categoryCode("CONSULTATION")
            .departmentId(deptId)
            .practitionerId(practitionerId)
            .reservationDate(LocalDate.of(2026, 10, 20))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 30))
            .statusId(UUID.randomUUID())
            .statusCode("PENDING_PAYMENT")
            .statusName("Pending Payment")
            .badgeColorHex("#F59E0B")
            .isPaid(false)
            .reasonForVisit("Fever")
            .notes("Notes")
            .receptionistStaffId(UUID.randomUUID())
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();

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
  void markAsPaid_whenBiller_shouldReturnOk() throws Exception {
    UUID resId = UUID.randomUUID();
    UUID invoiceId = UUID.randomUUID();

    ReservationDTO.Response res =
        ReservationDTO.Response.builder()
            .id(resId)
            .reservationNumber("CONS-20261020-001")
            .patientId(UUID.randomUUID())
            .patientName("John Doe")
            .patientPhone("+212600000000")
            .categoryId(UUID.randomUUID())
            .categoryName("Consultation")
            .categoryCode("CONSULTATION")
            .departmentId(UUID.randomUUID())
            .practitionerId(UUID.randomUUID())
            .reservationDate(LocalDate.of(2026, 10, 20))
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(9, 30))
            .statusId(UUID.randomUUID())
            .statusCode("PAID")
            .statusName("Paid / Ready for Queue")
            .badgeColorHex("#3B82F6")
            .isPaid(true)
            .billingInvoiceId(invoiceId)
            .reasonForVisit("Fever")
            .notes("Notes")
            .receptionistStaffId(UUID.randomUUID())
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();

    when(reservationService.markAsPaid(resId, invoiceId)).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/reservations/{id}/pay", resId)
                .param("invoiceId", invoiceId.toString())
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_BILLER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isPaid").value(true))
        .andExpect(jsonPath("$.statusCode").value("PAID"));
  }
}
