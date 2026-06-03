package com.hms.notification_service.controller;

import com.hms.notification_service.dto.*;
import com.hms.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {
  private final NotificationService service;

  @GetMapping
  @Operation(operationId = "listMyNotifications")
  public PageResponse<NotificationResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
    return service.list(
        UUID.fromString(jwt.getSubject()),
        PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
  }

  @GetMapping("/unread-count")
  @Operation(operationId = "getMyUnreadNotificationCount")
  public UnreadCountResponse unread(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
    return service.unreadCount(UUID.fromString(jwt.getSubject()));
  }

  @PatchMapping("/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(operationId = "markMyNotificationRead")
  public void markRead(
      @PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
    service.markRead(id, UUID.fromString(jwt.getSubject()));
  }

  @PatchMapping("/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(operationId = "markAllMyNotificationsRead")
  public void markAllRead(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
    service.markAllRead(UUID.fromString(jwt.getSubject()));
  }
}
