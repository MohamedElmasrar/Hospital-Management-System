package com.hms.hr_service.exception;

public class MedicalSpecialtyNotFoundException extends RuntimeException {

  public MedicalSpecialtyNotFoundException() {
    super("Medical Specialty Not Found");
  }

  public MedicalSpecialtyNotFoundException(String message) {
    super(message);
  }
}
