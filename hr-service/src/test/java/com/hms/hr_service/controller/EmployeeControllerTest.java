package com.hms.hr_service.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.hr_service.dto.Employee.CreateContactPointRequestDTO;
import com.hms.hr_service.dto.Employee.PostalAddressDTO;
import com.hms.hr_service.dto.Employee.UpdateEmployeeRequestDTO;
import com.hms.hr_service.enums.AddressType;
import com.hms.hr_service.enums.ContactType;
import com.hms.hr_service.enums.EmploymentStatus;
import com.hms.hr_service.exception.GlobalExceptionHandler;
import com.hms.hr_service.security.HrAccessService;
import com.hms.hr_service.security.SecurityConfig;
import com.hms.hr_service.service.EmployeeService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class EmployeeControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private EmployeeService employeeService;
  @MockitoBean private HrAccessService hrAccessService;

  @Test
  void updateEmployee_whenAdmin_shouldSucceed() throws Exception {
    UUID empId = UUID.randomUUID();
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "John", null, "Doe", null, null, null, EmploymentStatus.Active);

    mockMvc
        .perform(
            put("/employees/{id}", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updateEmployee_whenHrHead_shouldSucceed() throws Exception {
    UUID empId = UUID.randomUUID();
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "John", null, "Doe", null, null, null, EmploymentStatus.Active);

    mockMvc
        .perform(
            put("/employees/{id}", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_HR_HEAD")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updateEmployee_whenSelfEmployee_shouldSucceed() throws Exception {
    UUID empId = UUID.randomUUID();
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "John", null, "Doe", null, null, null, EmploymentStatus.Active);

    mockMvc
        .perform(
            put("/employees/{id}", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(empId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updateEmployee_whenOtherEmployee_shouldReturnForbidden() throws Exception {
    UUID empId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "John", null, "Doe", null, null, null, EmploymentStatus.Active);

    mockMvc
        .perform(
            put("/employees/{id}", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(otherUserId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateEmployee_whenDoctor_shouldReturnForbidden() throws Exception {
    UUID empId = UUID.randomUUID();
    UpdateEmployeeRequestDTO request =
        new UpdateEmployeeRequestDTO(
            "John", null, "Doe", null, null, null, EmploymentStatus.Active);

    mockMvc
        .perform(
            put("/employees/{id}", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(empId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addContactPoint_whenSelf_shouldSucceed() throws Exception {
    UUID empId = UUID.randomUUID();
    CreateContactPointRequestDTO request =
        new CreateContactPointRequestDTO(ContactType.Mobile, "555-1234", null, true);

    mockMvc
        .perform(
            post("/employees/{id}/contact-points", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(empId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void addContactPoint_whenOtherEmployee_shouldReturnForbidden() throws Exception {
    UUID empId = UUID.randomUUID();
    CreateContactPointRequestDTO request =
        new CreateContactPointRequestDTO(ContactType.Mobile, "555-1234", null, true);

    mockMvc
        .perform(
            post("/employees/{id}/contact-points", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateAddress_whenSelf_shouldSucceed() throws Exception {
    UUID empId = UUID.randomUUID();
    PostalAddressDTO request =
        new PostalAddressDTO(null, AddressType.Work, "123 Main St", "City", "State", "12345", "US");

    mockMvc
        .perform(
            put("/employees/{id}/address", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(empId.toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updateAddress_whenOtherEmployee_shouldReturnForbidden() throws Exception {
    UUID empId = UUID.randomUUID();
    PostalAddressDTO request =
        new PostalAddressDTO(null, AddressType.Work, "123 Main St", "City", "State", "12345", "US");

    mockMvc
        .perform(
            put("/employees/{id}/address", empId)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}
