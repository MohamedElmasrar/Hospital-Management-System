package com.hms.hr_service.exception;

public class EmployeeNotFoundException extends RuntimeException {

  public EmployeeNotFoundException() {
    super("Employee Not Found");
  }

  public EmployeeNotFoundException(String message) {
    super(message);
  }
}
