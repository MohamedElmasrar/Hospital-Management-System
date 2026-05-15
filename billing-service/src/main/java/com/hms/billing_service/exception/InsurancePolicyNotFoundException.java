package com.hms.billing_service.exception;

public class InsurancePolicyNotFoundException extends RuntimeException {
  public InsurancePolicyNotFoundException(String message) {
    super(message);
  }
}

