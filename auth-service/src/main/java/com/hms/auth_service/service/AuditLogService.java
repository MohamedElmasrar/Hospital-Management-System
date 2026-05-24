package com.hms.auth_service.service;

import com.hms.auth_service.dto.admin.AuditLogResponse;
import com.hms.auth_service.model.AuditLog;
import com.hms.auth_service.repository.AuditLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {
  private final AuditLogRepository auditLogRepository;

  public void record(UUID actorUserId, String action, UUID targetUserId) {
    AuditLog log = new AuditLog();
    log.setActorUserId(actorUserId);
    log.setAction(action);
    log.setTargetUserId(targetUserId);
    auditLogRepository.save(log);
  }

  public List<AuditLogResponse> findAll() {
    return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(log -> new AuditLogResponse(
            log.getId(), log.getActorUserId(), log.getAction(), log.getTargetUserId(),
            log.getCreatedAt()))
        .toList();
  }
}
