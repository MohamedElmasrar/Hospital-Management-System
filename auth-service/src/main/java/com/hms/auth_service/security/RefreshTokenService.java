package com.hms.auth_service.security;

import com.hms.auth_service.config.JwtProperties;
import com.hms.auth_service.exception.InvalidRefreshTokenException;
import com.hms.auth_service.model.RefreshToken;
import com.hms.auth_service.model.User;
import com.hms.auth_service.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  public RefreshToken findAndValidateRefreshToken(String rawToken) {

    RefreshToken refreshToken =
        refreshTokenRepository
            .findWithUserByTokenHash(hashToken(rawToken))
            .orElseThrow(InvalidRefreshTokenException::new);

    if (refreshToken.isRevoked()) {
      throw new InvalidRefreshTokenException();
    }

    if (refreshToken.getValidUntil() == null
        || refreshToken.getValidUntil().isBefore(LocalDateTime.now())) {
      throw new InvalidRefreshTokenException();
    }

    return refreshToken;
  }

  public static String hashToken(String rawToken) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public void revokeRefreshToken(String rawToken) {
    refreshTokenRepository
        .findByTokenHash(hashToken(rawToken))
        .ifPresent(
            token -> {
              token.setRevoked(true);
              refreshTokenRepository.save(token);
            });
  }

  public void createRefreshToken(User user, String rawRefreshToken) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(hashToken(rawRefreshToken));
    refreshToken.setValidUntil(
        LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpiryDays()));
    refreshTokenRepository.save(refreshToken);
  }
}
