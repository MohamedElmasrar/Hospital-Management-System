package com.hms.auth_service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.hms.auth_service.config.JwtProperties;
import com.hms.auth_service.enums.UserRole;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

  @Mock private JwtEncoder jwtEncoder;
  @Mock private JwtDecoder jwtDecoder;
  @Mock private JwtProperties jwtProperties;

  @InjectMocks private JwtTokenService jwtTokenService;

  @Test
  void generateAccessToken_validInput_returnsTokenValueAndSetsCorrectClaims() {
    UUID userId = UUID.randomUUID();
    UserRole role = UserRole.ADMIN;
    long expirySeconds = 3600L;

    given(jwtProperties.accessTokenExpirySeconds()).willReturn(expirySeconds);

    Jwt mockedJwt = mock(Jwt.class);
    given(mockedJwt.getTokenValue()).willReturn("mocked-jwt-token-value");

    ArgumentCaptor<JwtEncoderParameters> parametersCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    given(jwtEncoder.encode(parametersCaptor.capture())).willReturn(mockedJwt);

    String token = jwtTokenService.generateAccessToken(userId, role);

    assertThat(token).isEqualTo("mocked-jwt-token-value");

    JwtEncoderParameters capturedParameters = parametersCaptor.getValue();
    assertThat(capturedParameters.getJwsHeader().getAlgorithm()).isEqualTo(MacAlgorithm.HS256);
    assertThat(capturedParameters.getClaims().getSubject()).isEqualTo(userId.toString());
    assertThat(capturedParameters.getClaims().getClaimAsString("role")).isEqualTo(role.name());

    Instant issuedAt = capturedParameters.getClaims().getIssuedAt();
    Instant expiresAt = capturedParameters.getClaims().getExpiresAt();
    assertThat(issuedAt).isNotNull();
    assertThat(expiresAt).isNotNull();
    assertThat(expiresAt).isEqualTo(issuedAt.plusSeconds(expirySeconds));
  }

  @Test
  void generateRefreshToken_returnsValid64HexCharacters() {
    String refreshToken = jwtTokenService.generateRefreshToken();

    assertThat(refreshToken).isNotNull().hasSize(64).matches("^[0-9a-f]{64}$");
  }

  @Test
  void generateRefreshToken_successiveCalls_generateUniqueTokens() {
    String token1 = jwtTokenService.generateRefreshToken();
    String token2 = jwtTokenService.generateRefreshToken();

    assertThat(token1).isNotEqualTo(token2);
  }

  @Test
  void validateToken_validToken_returnsTrue() {
    String token = "valid-token";
    Jwt mockedJwt = mock(Jwt.class);
    given(jwtDecoder.decode(token)).willReturn(mockedJwt);

    boolean result = jwtTokenService.validateToken(token);

    assertThat(result).isTrue();
    verify(jwtDecoder).decode(token);
  }

  @Test
  void validateToken_invalidToken_returnsFalse() {
    String token = "invalid-token";
    given(jwtDecoder.decode(token)).willThrow(new BadJwtException("Token invalid or expired"));

    boolean result = jwtTokenService.validateToken(token);

    assertThat(result).isFalse();
    verify(jwtDecoder).decode(token);
  }
}
