package com.hms.auth_service.enums;

public enum UserRole {
  ADMIN,
  // Retains the old database ordinal; accounts at this position cannot sign in.
  DISABLED,
  DOCTOR,
  EMPLOYEE,
  RECEPTIONIST,
  HR_HEAD
}
