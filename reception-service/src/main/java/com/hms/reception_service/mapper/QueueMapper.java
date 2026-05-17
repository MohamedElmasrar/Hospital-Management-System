package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.queue.QueueDTO;
import com.hms.reception_service.model.DailyQueue;
import com.hms.reception_service.model.QueueTicket;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QueueMapper {

  @Mapping(source = "room.id", target = "roomId")
  @Mapping(source = "room.name", target = "roomName")
  QueueDTO.DailyQueueResponse toDailyQueueResponse(DailyQueue queue);

  @Mapping(source = "queue.id", target = "queueId")
  @Mapping(source = "reservation.id", target = "reservationId")
  @Mapping(source = "priority.id", target = "priorityId")
  @Mapping(source = "priority.name", target = "priorityName")
  @Mapping(source = "priority.weight", target = "priorityWeight")
  @Mapping(
      target = "patientName",
      expression =
          "java(ticket.getReservation() != null && ticket.getReservation().getPatient() != null ? ticket.getReservation().getPatient().getFirstName() + \" \" + ticket.getReservation().getPatient().getLastName() : null)")
  @Mapping(
      target = "roomName",
      expression =
          "java(ticket.getReservation() != null && ticket.getReservation().getRoom() != null ? ticket.getReservation().getRoom().getName() : null)")
  QueueDTO.TicketResponse toTicketResponse(QueueTicket ticket);

  List<QueueDTO.TicketResponse> toTicketResponseList(List<QueueTicket> tickets);
}
