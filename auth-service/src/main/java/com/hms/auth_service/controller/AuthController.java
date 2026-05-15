package com.hms.auth_service.controller;

import com.hms.auth_service.config.JwtProperties;
import com.hms.auth_service.dto.login.LoginRequestDTO;
import com.hms.auth_service.dto.login.LoginResponseDTO;
import com.hms.auth_service.dto.refresh.RefreshAccessTokenResponseDTO;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.service.AuthService;
import com.hms.auth_service.service.AuthService.LoginResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

  private final JwtProperties jwtProperties;
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDTO> login(
      @Valid @RequestBody LoginRequestDTO request, HttpServletResponse response) {

    LoginResult result = authService.login(request);

    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", result.rawRefreshToken())
            .httpOnly(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(
                (long) jwtProperties.refreshTokenExpiryDays() * 24 * 60 * 60) // 1 day in seconds
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok(new LoginResponseDTO(result.accessToken()));
  }

  @GetMapping("/refresh")
  public ResponseEntity<RefreshAccessTokenResponseDTO> refresh(
      @CookieValue(name = "refreshToken") String rawToken) {
    String accessToken = authService.refresh(rawToken);

    return ResponseEntity.ok(new RefreshAccessTokenResponseDTO(accessToken));
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
    authService.registerNewUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refreshToken", required = false) String rawToken,
      HttpServletResponse response) {
    authService.logout(rawToken);

    ResponseCookie clearCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return authService.validateToken(authHeader.substring(7))
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
