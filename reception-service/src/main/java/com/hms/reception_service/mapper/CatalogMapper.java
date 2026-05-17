package com.hms.reception_service.mapper;

import com.hms.reception_service.dto.catalog.DiagnosticModalityDTO;
import com.hms.reception_service.dto.catalog.ExaminationRoomDTO;
import com.hms.reception_service.dto.catalog.QueuePriorityLevelDTO;
import com.hms.reception_service.dto.catalog.ReceptionStationDTO;
import com.hms.reception_service.dto.catalog.ReservationCategoryDTO;
import com.hms.reception_service.dto.catalog.ReservationStatusDTO;
import com.hms.reception_service.model.DiagnosticModality;
import com.hms.reception_service.model.ExaminationRoom;
import com.hms.reception_service.model.QueuePriorityLevel;
import com.hms.reception_service.model.ReceptionStation;
import com.hms.reception_service.model.ReservationCategory;
import com.hms.reception_service.model.ReservationStatus;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CatalogMapper {

  // Category
  ReservationCategoryDTO.Response toCategoryResponse(ReservationCategory entity);

  List<ReservationCategoryDTO.Response> toCategoryResponseList(List<ReservationCategory> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "requiresPhysician", defaultExpression = "java(Boolean.TRUE)")
  @Mapping(target = "requiresPrescription", defaultExpression = "java(Boolean.FALSE)")
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  ReservationCategory toCategoryEntity(ReservationCategoryDTO.Request req);

  // Modality
  DiagnosticModalityDTO.Response toModalityResponse(DiagnosticModality entity);

  List<DiagnosticModalityDTO.Response> toModalityResponseList(List<DiagnosticModality> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "defaultDurationMinutes", defaultExpression = "java(30)")
  @Mapping(target = "requiresContrastScreening", defaultExpression = "java(Boolean.FALSE)")
  @Mapping(target = "requiresSafetyChecklist", defaultExpression = "java(Boolean.FALSE)")
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  DiagnosticModality toModalityEntity(DiagnosticModalityDTO.Request req);

  // Examination Room
  @Mapping(source = "modality.id", target = "modalityId")
  @Mapping(source = "modality.name", target = "modalityName")
  ExaminationRoomDTO.Response toRoomResponse(ExaminationRoom entity);

  List<ExaminationRoomDTO.Response> toRoomResponseList(List<ExaminationRoom> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "modality", ignore = true)
  @Mapping(target = "dateCreated", ignore = true)
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  ExaminationRoom toRoomEntity(ExaminationRoomDTO.Request req);

  // Reception Station
  @Mapping(source = "category.id", target = "categoryId")
  @Mapping(source = "category.name", target = "categoryName")
  ReceptionStationDTO.Response toStationResponse(ReceptionStation entity);

  List<ReceptionStationDTO.Response> toStationResponseList(List<ReceptionStation> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  ReceptionStation toStationEntity(ReceptionStationDTO.Request req);

  // Queue Priority Level
  QueuePriorityLevelDTO.Response toPriorityResponse(QueuePriorityLevel entity);

  List<QueuePriorityLevelDTO.Response> toPriorityResponseList(List<QueuePriorityLevel> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "weight", defaultExpression = "java(10)")
  @Mapping(target = "badgeColorHex", defaultExpression = "java(\"#10B981\")")
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  QueuePriorityLevel toPriorityEntity(QueuePriorityLevelDTO.Request req);

  // Reservation Status
  ReservationStatusDTO.Response toStatusResponse(ReservationStatus entity);

  List<ReservationStatusDTO.Response> toStatusResponseList(List<ReservationStatus> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "badgeColorHex", defaultExpression = "java(\"#6B7280\")")
  @Mapping(target = "allowsQueueing", defaultExpression = "java(Boolean.FALSE)")
  @Mapping(target = "isTerminal", defaultExpression = "java(Boolean.FALSE)")
  @Mapping(target = "isActive", defaultExpression = "java(Boolean.TRUE)")
  ReservationStatus toStatusEntity(ReservationStatusDTO.Request req);
}
