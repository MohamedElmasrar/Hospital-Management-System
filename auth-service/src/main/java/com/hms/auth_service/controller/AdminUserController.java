package com.hms.auth_service.controller;

import com.hms.auth_service.dto.admin.AuditLogResponse;
import com.hms.auth_service.dto.admin.UpdateUserRoleRequest;
import com.hms.auth_service.dto.admin.UserResponse;
import com.hms.auth_service.mapper.UserMapper;
import com.hms.auth_service.service.AuditLogService;
import com.hms.auth_service.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final UserService userService;
  private final AuditLogService auditLogService;
  private final UserMapper userMapper;

  @GetMapping
  public ResponseEntity<List<UserResponse>> listUsers() {
    return ResponseEntity.ok(userService.findAll());
  }

  @PatchMapping("/{id}/role")
  public ResponseEntity<UserResponse> updateRole(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRoleRequest request,
      @AuthenticationPrincipal Jwt principal) {
    UserResponse response = userMapper.toResponse(userService.updateRole(id, request.role()));
    auditLogService.record(UUID.fromString(principal.getSubject()), "USER_ROLE_UPDATED", id);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<UserResponse> updateActive(
      @PathVariable UUID id, @RequestParam boolean active, @AuthenticationPrincipal Jwt principal) {
    UserResponse response = userMapper.toResponse(userService.updateActive(id, active));
    auditLogService.record(
        UUID.fromString(principal.getSubject()),
        active ? "USER_ACTIVATED" : "USER_DEACTIVATED",
        id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/audit-logs")
  public ResponseEntity<List<AuditLogResponse>> auditLogs() {
    return ResponseEntity.ok(auditLogService.findAll());
  }
}
