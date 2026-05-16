package com.hms.billing_service.exception;

public class ServiceCatalogNotFoundException extends RuntimeException {
  public ServiceCatalogNotFoundException(String message) {
    super(message);
  }
}
