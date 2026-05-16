package com.hms.billing_service.exception;

public class BillableOrderNotFoundException extends RuntimeException {
  public BillableOrderNotFoundException(String message) {
    super(message);
  }
}
