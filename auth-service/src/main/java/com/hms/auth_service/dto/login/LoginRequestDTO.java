package com.hms.auth_service.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
    @NotBlank(message = "The email is required")
        @Email(message = "The email has to be a valid email")
        String email,
    @Size(min = 8, message = "The password has to be at least 8 characters") String password) {}
