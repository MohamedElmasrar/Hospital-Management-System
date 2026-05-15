package com.hms.billing_service.exception;

public class UnauthorizedOperationException extends RuntimeException {
  public UnauthorizedOperationException(String message) {
    super(message);
  }
}

