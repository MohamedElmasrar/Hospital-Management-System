package com.hms.billing_service.exception;

public class InsuranceProviderNotFoundException extends RuntimeException {
  public InsuranceProviderNotFoundException(String message) {
    super(message);
  }
}

