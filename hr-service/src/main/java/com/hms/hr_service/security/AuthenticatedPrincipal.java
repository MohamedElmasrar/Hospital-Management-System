package com.hms.hr_service.security;

import com.hms.hr_service.enums.UserRole;
import java.util.UUID;

public record AuthenticatedPrincipal(UUID userId, UserRole role) {}
