package com.hms.auth_service.service;

import com.hms.auth_service.dto.login.LoginRequestDTO;
import com.hms.auth_service.exception.InvalidCredentialsException;
import com.hms.auth_service.model.RefreshToken;
import com.hms.auth_service.model.User;
import com.hms.auth_service.security.JwtTokenService;
import com.hms.auth_service.security.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private final JwtTokenService jwtTokenService;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;
  private final BCryptPasswordEncoder passwordEncoder;

  public record LoginResult(String accessToken, String rawRefreshToken) {}

  public LoginResult login(LoginRequestDTO request) {
    User user =
        userService.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getRole());

    String rawRefreshToken = jwtTokenService.generateRefreshToken();

    return new LoginResult(accessToken, rawRefreshToken);
  }

  @Transactional
  public String refresh(String rawToken) {
    RefreshToken refreshToken = refreshTokenService.findAndValidateRefreshToken(rawToken);

    User user = refreshToken.getUser();

    return jwtTokenService.generateAccessToken(user.getId(), user.getRole());
  }
}
