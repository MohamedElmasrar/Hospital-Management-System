package com.hms.auth_service.exception;

public class UserWithTheSameEmailAlreadyExistsException extends RuntimeException {
  public UserWithTheSameEmailAlreadyExistsException() {
    super("User with the same email already exists");
  }
}
