package com.hms.billing_service.exception;

public class InvoiceNotFoundException extends RuntimeException {
  public InvoiceNotFoundException(String message) {
    super(message);
  }
}
