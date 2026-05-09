package com.hms.auth_service.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @Email(message = "The email has to be a valid email") String email,
    @Size(min = 8, max = 50, message = "The password must be at least 8 characters")
        String password) {}
