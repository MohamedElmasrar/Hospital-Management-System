package com.hms.hr_service.exception;

public class MedicalOrganizationNotFoundException extends RuntimeException {

  public MedicalOrganizationNotFoundException() {
    super("Medical Organization Not Found");
  }
}
