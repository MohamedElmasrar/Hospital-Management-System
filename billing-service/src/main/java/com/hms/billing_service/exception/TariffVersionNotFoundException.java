package com.hms.billing_service.exception;

public class TariffVersionNotFoundException extends RuntimeException {
  public TariffVersionNotFoundException(String message) {
    super(message);
  }
}
