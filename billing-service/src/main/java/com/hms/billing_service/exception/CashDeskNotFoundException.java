package com.hms.billing_service.exception;

public class CashDeskNotFoundException extends RuntimeException {
  public CashDeskNotFoundException(String message) {
    super(message);
  }
}

