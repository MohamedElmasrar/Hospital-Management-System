package com.hms.auth_service.dto.admin;

import com.hms.auth_service.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull UserRole role) {}
