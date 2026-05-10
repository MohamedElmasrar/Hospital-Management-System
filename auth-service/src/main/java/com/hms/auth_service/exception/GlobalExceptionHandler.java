package com.hms.auth_service.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(
      InvalidCredentialsException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", "Invalid Credentials"));
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<Map<String, String>> handleInvalidRefreshTokenException(
      InvalidRefreshTokenException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", "Invalid Refresh Token"));
  }

  @ExceptionHandler(UserWithTheSameEmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, String>> handleUserWithTheSameEmailAlreadyExistsException(
      UserWithTheSameEmailAlreadyExistsException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", "User with this email already exists"));
  }
}
