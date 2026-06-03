package com.hms.reception_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReceptionServiceApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {}

  @Test
  void openApiDocsEndpointReturnsOk() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi").exists())
        .andExpect(
            jsonPath("$.components.schemas.AvailableSlot.properties.startTime.type")
                .value("string"))
        .andExpect(
            jsonPath("$.components.schemas.AvailableSlot.properties.endTime.format").value("time"))
        .andExpect(
            jsonPath("$.components.schemas.Response.properties.startTime.type").value("string"))
        .andExpect(
            jsonPath("$.components.schemas.CreateRequest.properties.endTime.type").value("string"));
  }
}
