package com.hms.auth_service.mapper;

import com.hms.auth_service.dto.admin.AuditLogResponse;
import com.hms.auth_service.model.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {
  AuditLogResponse toResponse(AuditLog auditLog);
}
