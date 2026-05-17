package com.hms.reception_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.reception_service.dto.catalog.ReservationCategoryDTO;
import com.hms.reception_service.exception.GlobalExceptionHandler;
import com.hms.reception_service.security.SecurityConfig;
import com.hms.reception_service.service.AdminCatalogService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCatalogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminCatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private AdminCatalogService catalogService;

  @Test
  void getCategories_whenAdmin_shouldReturnList() throws Exception {
    ReservationCategoryDTO.Response cat =
        new ReservationCategoryDTO.Response(
            UUID.randomUUID(),
            "CONSULTATION",
            "Consultation",
            "Doctor visit",
            true,
            false,
            true,
            new Date());

    when(catalogService.getAllCategories()).thenReturn(List.of(cat));

    mockMvc
        .perform(
            get("/api/reception/admin/categories")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").value("CONSULTATION"));
  }

  @Test
  void createCategory_whenAdmin_shouldReturnCreated() throws Exception {
    ReservationCategoryDTO.Request req =
        new ReservationCategoryDTO.Request("LAB", "Lab", "Tests", false, true, true);
    ReservationCategoryDTO.Response res =
        new ReservationCategoryDTO.Response(
            UUID.randomUUID(), "LAB", "Lab", "Tests", false, true, true, new Date());

    when(catalogService.createCategory(any())).thenReturn(res);

    mockMvc
        .perform(
            post("/api/reception/admin/categories")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated());
  }

  @Test
  void getCategories_whenNotAdmin_shouldReturnForbidden() throws Exception {
    mockMvc
        .perform(
            get("/api/reception/admin/categories")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
        .andExpect(status().isForbidden());
  }
}
