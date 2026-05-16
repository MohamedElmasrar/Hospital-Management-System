package com.hms.billing_service.exception;

public class InsuranceClaimNotFoundException extends RuntimeException {
  public InsuranceClaimNotFoundException(String message) {
    super(message);
  }
}
