package com.hms.hr_service.exception;

public class PhysicianNotFoundException extends RuntimeException {

  public PhysicianNotFoundException() {
    super("Physician Not Found");
  }

  public PhysicianNotFoundException(String message) {
    super(message);
  }
}
