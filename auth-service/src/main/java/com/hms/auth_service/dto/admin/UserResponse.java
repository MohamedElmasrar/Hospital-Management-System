package com.hms.auth_service.dto.admin;

import com.hms.auth_service.enums.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String email, UserRole role, boolean active) {}
