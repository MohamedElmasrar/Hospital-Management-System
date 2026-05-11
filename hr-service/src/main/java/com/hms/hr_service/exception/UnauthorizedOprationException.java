package com.hms.hr_service.exception;

public class UnauthorizedOprationException extends UnauthorizedOperationException {

  public UnauthorizedOprationException() {
    super("Unauthorized operation");
  }

  public UnauthorizedOprationException(String message) {
    super(message);
  }
}
