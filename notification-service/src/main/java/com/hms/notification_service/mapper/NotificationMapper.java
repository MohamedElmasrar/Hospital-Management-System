package com.hms.notification_service.mapper;

import com.hms.notification_service.dto.*;
import com.hms.notification_service.model.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  @Mapping(target = "id", source = "eventId")
  @Mapping(target = "createdAt", source = "occurredAt")
  @Mapping(target = "readAt", ignore = true)
  Notification toEntity(NotificationEvent event);

  NotificationResponse toResponse(Notification notification);
}
