package com.hms.auth_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.auth_service.config.JwtProperties;
import com.hms.auth_service.dto.login.LoginRequestDTO;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.exception.GlobalExceptionHandler;
import com.hms.auth_service.exception.InvalidCredentialsException;
import com.hms.auth_service.exception.InvalidRefreshTokenException;
import com.hms.auth_service.exception.UserWithTheSameEmailAlreadyExistsException;
import com.hms.auth_service.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private JwtProperties jwtProperties;
  @MockitoBean private AuthService authService;

  // ===================== Login Tests =====================

  @Test
  void login_WhenCredentialsAreValid_ShouldReturn200andTokens() throws Exception {
    LoginRequestDTO request = new LoginRequestDTO("testuser@example.com", "valid_password");
    AuthService.LoginResult mockResult =
        new AuthService.LoginResult("jwt-access-token", "raw-refresh-token");
    given(authService.login(any(LoginRequestDTO.class))).willReturn(mockResult);
    given(jwtProperties.refreshTokenExpiryDays()).willReturn(7);

    String cookieName = "refreshToken", cookieValue = "raw-refresh-token";

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
        .andExpect(jsonPath("$.%s".formatted(cookieName)).doesNotExist())
        .andExpect(cookie().exists(cookieName))
        .andExpect(cookie().value(cookieName, cookieValue))
        .andExpect(cookie().httpOnly(cookieName, true))
        .andExpect(cookie().path(cookieName, "/"));

    verify(authService).login(request);
    verify(jwtProperties).refreshTokenExpiryDays();
  }

  @Test
  void login_WhenCredentialsAreInvalid_ShouldReturn400BadRequest() throws Exception {
    LoginRequestDTO request = new LoginRequestDTO("testuser@example.com", "wrong_password");
    given(authService.login(any(LoginRequestDTO.class)))
        .willThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid Credentials"));
  }

  @Test
  void login_WhenEmailIsInvalid_ShouldReturn400BadRequest() throws Exception {
    LoginRequestDTO request = new LoginRequestDTO("not-an-email", "valid_password");

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.email").exists());
  }

  @Test
  void login_WhenPasswordIsTooShort_ShouldReturn400BadRequest() throws Exception {
    LoginRequestDTO request = new LoginRequestDTO("testuser@example.com", "short");

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.password").exists());
  }

  // ===================== Refresh Tests =====================

  @Test
  void refresh_WhenRefreshTokenIsValid_ShouldReturn200AndNewAccessToken() throws Exception {
    given(authService.refresh("valid-refresh-token")).willReturn("new-jwt-access-token");

    Cookie cookie = new Cookie("refreshToken", "valid-refresh-token");

    mockMvc
        .perform(get("/refresh").cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-jwt-access-token"));

    verify(authService).refresh("valid-refresh-token");
  }

  @Test
  void refresh_WhenRefreshTokenIsMissing_ShouldReturn400BadRequest() throws Exception {
    mockMvc.perform(get("/refresh")).andExpect(status().isBadRequest());
  }

  @Test
  void refresh_WhenRefreshTokenIsInvalid_ShouldReturn400BadRequest() throws Exception {
    given(authService.refresh("invalid-token")).willThrow(new InvalidRefreshTokenException());

    Cookie cookie = new Cookie("refreshToken", "invalid-token");

    mockMvc
        .perform(get("/refresh").cookie(cookie))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid Refresh Token"));
  }

  // ===================== Register Tests =====================

  @Test
  void register_WhenRequestIsValid_ShouldReturn201Created() throws Exception {
    RegisterRequestDTO request = new RegisterRequestDTO("newuser@example.com", "password123");

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(authService).registerNewUser(request);
  }

  @Test
  void register_WhenEmailAlreadyExists_ShouldReturn400BadRequest() throws Exception {
    RegisterRequestDTO request = new RegisterRequestDTO("existing@example.com", "password123");
    doThrow(new UserWithTheSameEmailAlreadyExistsException())
        .when(authService)
        .registerNewUser(any(RegisterRequestDTO.class));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("User with this email already exists"));
  }

  @Test
  void register_WhenValidationFails_ShouldReturn400BadRequest() throws Exception {
    RegisterRequestDTO request = new RegisterRequestDTO("invalid-email", "short");

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.email").exists())
        .andExpect(jsonPath("$.password").exists());
  }

  // ===================== Logout Tests =====================

  @Test
  void logout_WhenRefreshTokenCookieProvided_ShouldReturn204AndClearCookie() throws Exception {
    Cookie cookie = new Cookie("refreshToken", "raw-refresh-token");

    mockMvc
        .perform(post("/logout").cookie(cookie))
        .andExpect(status().isNoContent())
        .andExpect(cookie().value("refreshToken", ""))
        .andExpect(cookie().maxAge("refreshToken", 0))
        .andExpect(cookie().httpOnly("refreshToken", true))
        .andExpect(cookie().path("refreshToken", "/"));

    verify(authService).logout("raw-refresh-token");
  }

  @Test
  void logout_WhenRefreshTokenCookieNotProvided_ShouldReturn204AndClearCookie() throws Exception {
    mockMvc
        .perform(post("/logout"))
        .andExpect(status().isNoContent())
        .andExpect(cookie().value("refreshToken", ""))
        .andExpect(cookie().maxAge("refreshToken", 0))
        .andExpect(cookie().httpOnly("refreshToken", true))
        .andExpect(cookie().path("refreshToken", "/"));

    verify(authService).logout(null);
  }

  // ===================== Validate Token Tests =====================

  @Test
  void validateToken_WhenTokenIsValid_ShouldReturn200Ok() throws Exception {
    given(authService.validateToken("valid-token")).willReturn(true);

    mockMvc
        .perform(post("/validate").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());

    verify(authService).validateToken("valid-token");
  }

  @Test
  void validateToken_WhenTokenIsInvalid_ShouldReturn401Unauthorized() throws Exception {
    given(authService.validateToken("invalid-token")).willReturn(false);

    mockMvc
        .perform(post("/validate").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());

    verify(authService).validateToken("invalid-token");
  }

  @Test
  void validateToken_WhenAuthHeaderDoesNotStartWithBearer_ShouldReturn401Unauthorized()
      throws Exception {
    mockMvc
        .perform(post("/validate").header("Authorization", "Basic 12345"))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(authService);
  }

  @Test
  void validateToken_WhenAuthHeaderIsMissing_ShouldReturn400BadRequest() throws Exception {
    mockMvc.perform(post("/validate")).andExpect(status().isBadRequest());

    verifyNoInteractions(authService);
  }
}
