package com.hms.auth_service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.hms.auth_service.exception.InvalidRefreshTokenException;
import com.hms.auth_service.model.RefreshToken;
import com.hms.auth_service.model.User;
import com.hms.auth_service.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokenService refreshTokenService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("user@example.com");
  }

  @Test
  void hashToken_returnsDeterministicSha256Hex() {
    String rawToken = "my-secret-refresh-token";
    String hash1 = RefreshTokenService.hashToken(rawToken);
    String hash2 = RefreshTokenService.hashToken(rawToken);

    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash1).hasSize(64).matches("^[0-9a-f]{64}$");

    String differentHash = RefreshTokenService.hashToken("another-token");
    assertThat(hash1).isNotEqualTo(differentHash);
  }

  @Test
  void findAndValidateRefreshToken_validActiveToken_returnsRefreshToken() {
    String rawToken = "valid-token";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setRevoked(false);
    refreshToken.setCreatedAt(LocalDateTime.now().plusDays(7));

    given(refreshTokenRepository.findWithUserByTokenHash(tokenHash))
        .willReturn(Optional.of(refreshToken));

    RefreshToken result = refreshTokenService.findAndValidateRefreshToken(rawToken);

    assertThat(result).isNotNull();
    assertThat(result.getTokenHash()).isEqualTo(tokenHash);
    assertThat(result.getUser()).isEqualTo(user);
    verify(refreshTokenRepository).findWithUserByTokenHash(tokenHash);
  }

  @Test
  void findAndValidateRefreshToken_tokenNotFound_throwsInvalidRefreshTokenException() {
    String rawToken = "missing-token";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    given(refreshTokenRepository.findWithUserByTokenHash(tokenHash)).willReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenService.findAndValidateRefreshToken(rawToken))
        .isInstanceOf(InvalidRefreshTokenException.class);

    verify(refreshTokenRepository).findWithUserByTokenHash(tokenHash);
  }

  @Test
  void findAndValidateRefreshToken_tokenRevoked_throwsInvalidRefreshTokenException() {
    String rawToken = "revoked-token";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setRevoked(true);
    refreshToken.setCreatedAt(LocalDateTime.now().plusDays(7));

    given(refreshTokenRepository.findWithUserByTokenHash(tokenHash))
        .willReturn(Optional.of(refreshToken));

    assertThatThrownBy(() -> refreshTokenService.findAndValidateRefreshToken(rawToken))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void findAndValidateRefreshToken_createdAtInPast_throwsInvalidRefreshTokenException() {
    String rawToken = "past-token";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setRevoked(false);
    refreshToken.setCreatedAt(LocalDateTime.now().minusMinutes(5));

    given(refreshTokenRepository.findWithUserByTokenHash(tokenHash))
        .willReturn(Optional.of(refreshToken));

    assertThatThrownBy(() -> refreshTokenService.findAndValidateRefreshToken(rawToken))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void revokeRefreshToken_tokenFound_marksRevokedAndSaves() {
    String rawToken = "token-to-revoke";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setTokenHash(tokenHash);
    refreshToken.setRevoked(false);

    given(refreshTokenRepository.findByTokenHash(tokenHash)).willReturn(Optional.of(refreshToken));

    refreshTokenService.revokeRefreshToken(rawToken);

    assertThat(refreshToken.isRevoked()).isTrue();
    verify(refreshTokenRepository).save(refreshToken);
  }

  @Test
  void revokeRefreshToken_tokenNotFound_doesNothing() {
    String rawToken = "non-existent-token";
    String tokenHash = RefreshTokenService.hashToken(rawToken);

    given(refreshTokenRepository.findByTokenHash(tokenHash)).willReturn(Optional.empty());

    refreshTokenService.revokeRefreshToken(rawToken);

    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  void createRefreshToken_validUserAndToken_createsAndSavesToken() {
    String rawToken = "new-refresh-token";
    String expectedHash = RefreshTokenService.hashToken(rawToken);

    refreshTokenService.createRefreshToken(user, rawToken);

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());

    RefreshToken savedToken = captor.getValue();
    assertThat(savedToken.getUser()).isEqualTo(user);
    assertThat(savedToken.getTokenHash()).isEqualTo(expectedHash);
    assertThat(savedToken.isRevoked()).isFalse();
  }
}
