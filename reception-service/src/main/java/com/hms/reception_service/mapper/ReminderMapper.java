package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.reminder.ReminderDTO;
import com.hms.reception_service.model.AppointmentReminder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReminderMapper {
  @Mapping(source = "reservation.id", target = "reservationId")
  ReminderDTO.Response toResponse(AppointmentReminder reminder);
}
