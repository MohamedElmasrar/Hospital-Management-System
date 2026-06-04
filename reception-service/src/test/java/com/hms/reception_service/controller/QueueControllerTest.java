package com.hms.reception_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.reception_service.dto.queue.QueueDTO;
import com.hms.reception_service.exception.GlobalExceptionHandler;
import com.hms.reception_service.security.SecurityConfig;
import com.hms.reception_service.service.QueueService;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QueueController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class QueueControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private QueueService queueService;

  @Test
  void issueTicket_whenEmployee_shouldReturnCreated() throws Exception {
    UUID resId = UUID.randomUUID();
    QueueDTO.IssueTicketRequest req =
        QueueDTO.IssueTicketRequest.builder().reservationId(resId).build();

    QueueDTO.TicketResponse res =
        QueueDTO.TicketResponse.builder()
            .id(UUID.randomUUID())
            .queueId(UUID.randomUUID())
            .reservationId(resId)
            .ticketCode("CONS-001")
            .sequenceNumber(1)
            .priorityId(UUID.randomUUID())
            .priorityName("Normal")
            .priorityWeight(10)
            .status("WAITING")
            .issuedAt(new Date())
            .patientName("John Doe")
            .roomName("Room 101")
            .build();

    when(queueService.issueTicket(any())).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/queues/tickets")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ticketCode").value("CONS-001"))
        .andExpect(jsonPath("$.status").value("WAITING"));
  }

  @Test
  void callNextTicket_whenDoctor_shouldReturnCalledTicket() throws Exception {
    UUID queueId = UUID.randomUUID();

    QueueDTO.TicketResponse res =
        QueueDTO.TicketResponse.builder()
            .id(UUID.randomUUID())
            .queueId(queueId)
            .reservationId(UUID.randomUUID())
            .ticketCode("CONS-001")
            .sequenceNumber(1)
            .priorityId(UUID.randomUUID())
            .priorityName("Normal")
            .priorityWeight(10)
            .status("CALLED")
            .issuedAt(new Date())
            .calledAt(new Date())
            .patientName("John Doe")
            .roomName("Room 101")
            .build();

    when(queueService.callNextTicket(queueId)).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/queues/{queueId}/call-next", queueId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CALLED"));
  }

  @Test
  void issueTicket_whenReceptionist_shouldReturnCreated() throws Exception {
    UUID resId = UUID.randomUUID();
    QueueDTO.IssueTicketRequest req =
        QueueDTO.IssueTicketRequest.builder().reservationId(resId).build();

    QueueDTO.TicketResponse res =
        QueueDTO.TicketResponse.builder()
            .id(UUID.randomUUID())
            .queueId(UUID.randomUUID())
            .reservationId(resId)
            .ticketCode("CONS-002")
            .sequenceNumber(2)
            .priorityId(UUID.randomUUID())
            .priorityName("Normal")
            .priorityWeight(10)
            .status("WAITING")
            .issuedAt(new Date())
            .patientName("Jane Doe")
            .roomName("Room 102")
            .build();

    when(queueService.issueTicket(any())).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/queues/tickets")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_RECEPTIONIST")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ticketCode").value("CONS-002"));
  }
}
