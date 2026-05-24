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
    QueueDTO.IssueTicketRequest req = new QueueDTO.IssueTicketRequest(resId, null);

    QueueDTO.TicketResponse res =
        new QueueDTO.TicketResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            resId,
            "CONS-001",
            1,
            UUID.randomUUID(),
            "Normal",
            10,
            "WAITING",
            new Date(),
            null,
            null,
            null,
            "John Doe",
            "Room 101");

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
        new QueueDTO.TicketResponse(
            UUID.randomUUID(),
            queueId,
            UUID.randomUUID(),
            "CONS-001",
            1,
            UUID.randomUUID(),
            "Normal",
            10,
            "CALLED",
            new Date(),
            new Date(),
            null,
            null,
            "John Doe",
            "Room 101");

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
    QueueDTO.IssueTicketRequest req = new QueueDTO.IssueTicketRequest(resId, null);

    QueueDTO.TicketResponse res =
        new QueueDTO.TicketResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            resId,
            "CONS-002",
            2,
            UUID.randomUUID(),
            "Normal",
            10,
            "WAITING",
            new Date(),
            null,
            null,
            null,
            "Jane Doe",
            "Room 102");

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
