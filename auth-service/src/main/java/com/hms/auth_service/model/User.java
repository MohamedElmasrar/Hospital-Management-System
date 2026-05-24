package com.hms.auth_service.model;

import com.hms.auth_service.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  UUID id;

  @Column(name = "email", nullable = false)
  String email;

  @Column(name = "password_hash", nullable = false)
  String passwordHash;

  @Column(name = "role")
  UserRole role = UserRole.EMPLOYEE;

  @Column(name = "active", nullable = false)
  boolean active = true;
}
