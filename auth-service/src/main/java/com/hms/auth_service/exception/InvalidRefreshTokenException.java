package com.hms.auth_service.exception;

public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException() {
    super("Invalid RefreshToken");
  }
}
