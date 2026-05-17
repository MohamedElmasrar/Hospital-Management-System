package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.schedule.ScheduleDTO;
import com.hms.reception_service.model.ServiceSchedule;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ScheduleMapper {

  @Mapping(source = "room.id", target = "roomId")
  @Mapping(source = "room.name", target = "roomName")
  ScheduleDTO.Response toResponse(ServiceSchedule schedule);

  List<ScheduleDTO.Response> toResponseList(List<ServiceSchedule> schedules);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "room", ignore = true)
  @Mapping(
      target = "slotDurationMinutes",
      defaultExpression =
          "java(req.slotDurationMinutes() != null ? req.slotDurationMinutes() : 30)")
  @Mapping(
      target = "isActive",
      defaultExpression = "java(req.isActive() != null ? req.isActive() : Boolean.TRUE)")
  ServiceSchedule toEntity(ScheduleDTO.CreateRequest req);
}
