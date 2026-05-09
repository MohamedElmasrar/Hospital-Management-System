package com.hms.auth_service.security;

import com.hms.auth_service.config.JwtProperties;
import com.hms.auth_service.enums.UserRole;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  @Autowired private final JwtEncoder jwtEncoder;
  @Autowired private final JwtProperties jwtProperties;

  public String generateAccessToken(UUID userId, UserRole userRole) {
    Instant now = Instant.now();

    JwtClaimsSet.Builder builder =
        JwtClaimsSet.builder()
            .subject(userId.toString())
            .claim("role", userRole.name())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.accessTokenExpirySeconds()));

    return jwtEncoder
        .encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), builder.build()))
        .getTokenValue();
  }

  public String generateRefreshToken() {
    byte[] bytes = new SecureRandom().generateSeed(32);

    return HexFormat.of().formatHex(bytes);
  }
}
