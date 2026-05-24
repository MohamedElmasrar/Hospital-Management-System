package com.hms.auth_service.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
    UUID id, UUID actorUserId, String action, UUID targetUserId, LocalDateTime createdAt) {}
