package com.hms.hr_service.exception;

public class UnauthorizedOperationException extends RuntimeException {

  public UnauthorizedOperationException() {
    super("Unauthorized operation");
  }

  public UnauthorizedOperationException(String message) {
    super(message);
  }
}
