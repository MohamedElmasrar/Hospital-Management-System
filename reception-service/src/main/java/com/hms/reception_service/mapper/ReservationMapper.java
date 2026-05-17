package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.model.DiagnosticReservationDetail;
import com.hms.reception_service.model.Reservation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservationMapper {

  @Mapping(source = "patient.id", target = "patientId")
  @Mapping(
      target = "patientName",
      expression =
          "java(reservation.getPatient() != null ? reservation.getPatient().getFirstName() + \" \" + reservation.getPatient().getLastName() : null)")
  @Mapping(source = "patient.phone", target = "patientPhone")
  @Mapping(source = "category.id", target = "categoryId")
  @Mapping(source = "category.name", target = "categoryName")
  @Mapping(source = "category.code", target = "categoryCode")
  @Mapping(source = "prescription.id", target = "prescriptionId")
  @Mapping(source = "room.id", target = "roomId")
  @Mapping(source = "room.name", target = "roomName")
  @Mapping(source = "status.id", target = "statusId")
  @Mapping(source = "status.code", target = "statusCode")
  @Mapping(source = "status.name", target = "statusName")
  @Mapping(source = "status.badgeColorHex", target = "badgeColorHex")
  @Mapping(source = "diagnosticDetail", target = "diagnosticDetail")
  ReservationDTO.Response toResponse(Reservation reservation);

  List<ReservationDTO.Response> toResponseList(List<Reservation> reservations);

  @Mapping(source = "modality.id", target = "modalityId")
  @Mapping(source = "modality.name", target = "modalityName")
  @Mapping(source = "modality.code", target = "modalityCode")
  ReservationDTO.DiagnosticDetailResponse toDiagnosticDetailResponse(
      DiagnosticReservationDetail detail);
}
