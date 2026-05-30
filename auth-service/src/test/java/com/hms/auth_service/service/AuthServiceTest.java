package com.hms.auth_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hms.auth_service.dto.login.LoginRequestDTO;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.enums.UserRole;
import com.hms.auth_service.exception.InvalidCredentialsException;
import com.hms.auth_service.exception.InvalidRefreshTokenException;
import com.hms.auth_service.messaging.AnalyticsEventPublisher;
import com.hms.auth_service.model.RefreshToken;
import com.hms.auth_service.model.User;
import com.hms.auth_service.security.JwtTokenService;
import com.hms.auth_service.security.RefreshTokenService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private RefreshTokenService refreshTokenService;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private JwtTokenService jwtTokenService;
  @Mock private UserService userService;
  @Mock private AnalyticsEventPublisher analyticsEventPublisher;

  @InjectMocks private AuthService authService;

  private User existingUser;

  @BeforeEach
  void setUp() {
    existingUser = new User();
    existingUser.setId(UUID.randomUUID());
    existingUser.setEmail("testuser@email.com");
    existingUser.setPasswordHash("$2a$10$hashedPasswordPlaceholder");
    existingUser.setRole(UserRole.EMPLOYEE);
  }

  @Test
  void login_validCredentials_returnsTokens() {
    LoginRequestDTO request = new LoginRequestDTO("testuser@email.com", "valid_password");

    given(userService.findByEmail(request.email())).willReturn(Optional.of(existingUser));
    given(passwordEncoder.matches("valid_password", existingUser.getPasswordHash()))
        .willReturn(true);
    given(jwtTokenService.generateAccessToken(existingUser.getId(), existingUser.getRole()))
        .willReturn("mocked-access-token");
    given(jwtTokenService.generateRefreshToken()).willReturn("raw-refresh-token-xyz");

    AuthService.LoginResult result = authService.login(request);

    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo("mocked-access-token");
    assertThat(result.rawRefreshToken()).isEqualTo("raw-refresh-token-xyz");

    verify(refreshTokenService).createRefreshToken(existingUser, "raw-refresh-token-xyz");
    verify(analyticsEventPublisher)
        .publishUserEvent("USER_LOGGED_IN", existingUser.getId(), existingUser.getRole());
  }

  @Test
  void login_invalidEmail_throwsInvalidCredentialsException() {
    LoginRequestDTO request = new LoginRequestDTO("wrongemail@email.com", "invalid_password");

    given(userService.findByEmail(request.email())).willReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(InvalidCredentialsException.class);

    verifyNoInteractions(passwordEncoder, jwtTokenService, refreshTokenService);
  }

  @Test
  void login_invalidPassword_throwsInvalidCredentialsException() {
    LoginRequestDTO request = new LoginRequestDTO("validemail@email.com", "invalid_password");

    given(userService.findByEmail(request.email())).willReturn(Optional.of(existingUser));
    given(passwordEncoder.matches(request.password(), existingUser.getPasswordHash()))
        .willReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(InvalidCredentialsException.class);

    verifyNoInteractions(jwtTokenService, refreshTokenService);
  }

  @Test
  void refresh_validRefreshToken_returnsNewAccessToken() {
    String rawRefreshToken = "valid-raw-refresh-token";
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(existingUser);

    given(refreshTokenService.findAndValidateRefreshToken(rawRefreshToken))
        .willReturn(refreshToken);
    given(jwtTokenService.generateAccessToken(existingUser.getId(), existingUser.getRole()))
        .willReturn("new-access-token");

    String result = authService.refresh(rawRefreshToken);

    assertThat(result).isEqualTo("new-access-token");
    verify(refreshTokenService).findAndValidateRefreshToken(rawRefreshToken);
    verify(jwtTokenService).generateAccessToken(existingUser.getId(), existingUser.getRole());
    verify(analyticsEventPublisher)
        .publishUserEvent("ACCESS_TOKEN_REFRESHED", existingUser.getId(), existingUser.getRole());
  }

  @Test
  void refresh_invalidRefreshToken_throwsInvalidRefreshTokenException() {
    String rawRefreshToken = "invalid-raw-refresh-token";

    given(refreshTokenService.findAndValidateRefreshToken(rawRefreshToken))
        .willThrow(new InvalidRefreshTokenException());

    assertThatThrownBy(() -> authService.refresh(rawRefreshToken))
        .isInstanceOf(InvalidRefreshTokenException.class);

    verifyNoInteractions(jwtTokenService);
  }

  @Test
  void logout_validToken_revokesRefreshToken() {
    String rawRefreshToken = "valid-raw-refresh-token";
    given(refreshTokenService.revokeRefreshToken(rawRefreshToken))
        .willReturn(Optional.of(existingUser));

    authService.logout(rawRefreshToken);

    verify(refreshTokenService).revokeRefreshToken(rawRefreshToken);
    verify(analyticsEventPublisher)
        .publishUserEvent("USER_LOGGED_OUT", existingUser.getId(), existingUser.getRole());
  }

  @Test
  void logout_nullToken_doesNothing() {
    authService.logout(null);

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  void logout_blankToken_doesNothing() {
    authService.logout("   ");

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  void logout_emptyToken_doesNothing() {
    authService.logout("");

    verifyNoInteractions(refreshTokenService);
  }

  @Test
  void registerNewUser_validRequest_delegatesToUserService() {
    RegisterRequestDTO request = new RegisterRequestDTO("newuser@email.com", "password123");

    authService.registerNewUser(request);

    verify(userService).registerNewUser(request);
  }

  @Test
  void validateToken_validToken_returnsTrue() {
    String token = "valid-token";
    given(jwtTokenService.validateToken(token)).willReturn(true);

    boolean isValid = authService.validateToken(token);

    assertThat(isValid).isTrue();
    verify(jwtTokenService).validateToken(token);
  }

  @Test
  void validateToken_invalidToken_returnsFalse() {
    String token = "invalid-token";
    given(jwtTokenService.validateToken(token)).willReturn(false);

    boolean isValid = authService.validateToken(token);

    assertThat(isValid).isFalse();
    verify(jwtTokenService).validateToken(token);
  }
}
