package com.hms.reception_service.service;

import com.hms.reception_service.dto.catalog.DiagnosticModalityDTO;
import com.hms.reception_service.dto.catalog.ExaminationRoomDTO;
import com.hms.reception_service.dto.catalog.QueuePriorityLevelDTO;
import com.hms.reception_service.dto.catalog.ReceptionStationDTO;
import com.hms.reception_service.dto.catalog.ReservationCategoryDTO;
import com.hms.reception_service.dto.catalog.ReservationStatusDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.mapper.CatalogMapper;
import com.hms.reception_service.model.DiagnosticModality;
import com.hms.reception_service.model.ExaminationRoom;
import com.hms.reception_service.model.QueuePriorityLevel;
import com.hms.reception_service.model.ReceptionStation;
import com.hms.reception_service.model.ReservationCategory;
import com.hms.reception_service.model.ReservationStatus;
import com.hms.reception_service.repository.DiagnosticModalityRepository;
import com.hms.reception_service.repository.ExaminationRoomRepository;
import com.hms.reception_service.repository.QueuePriorityLevelRepository;
import com.hms.reception_service.repository.ReceptionStationRepository;
import com.hms.reception_service.repository.ReservationCategoryRepository;
import com.hms.reception_service.repository.ReservationStatusRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCatalogService {

  private final ReservationCategoryRepository categoryRepository;
  private final DiagnosticModalityRepository modalityRepository;
  private final ReservationStatusRepository statusRepository;
  private final QueuePriorityLevelRepository priorityRepository;
  private final ExaminationRoomRepository roomRepository;
  private final ReceptionStationRepository stationRepository;
  private final HrGrpcClient hrGrpcClient;
  private final CatalogMapper catalogMapper;

  // --- Categories ---
  @Transactional(readOnly = true)
  public List<ReservationCategoryDTO.Response> getAllCategories() {
    return catalogMapper.toCategoryResponseList(categoryRepository.findAll());
  }

  @Transactional
  public ReservationCategoryDTO.Response createCategory(ReservationCategoryDTO.Request req) {
    if (categoryRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Category with code " + req.code() + " already exists");
    }
    ReservationCategory cat = catalogMapper.toCategoryEntity(req);
    return catalogMapper.toCategoryResponse(categoryRepository.save(cat));
  }

  // --- Modalities ---
  @Transactional(readOnly = true)
  public List<DiagnosticModalityDTO.Response> getAllModalities() {
    return catalogMapper.toModalityResponseList(modalityRepository.findAll());
  }

  @Transactional
  public DiagnosticModalityDTO.Response createModality(DiagnosticModalityDTO.Request req) {
    if (modalityRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Modality with code " + req.code() + " already exists");
    }
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException(
          "Department ID " + req.departmentId() + " not found in HR service");
    }
    DiagnosticModality mod = catalogMapper.toModalityEntity(req);
    return catalogMapper.toModalityResponse(modalityRepository.save(mod));
  }

  // --- Examination Rooms ---
  @Transactional(readOnly = true)
  public List<ExaminationRoomDTO.Response> getAllRooms() {
    return catalogMapper.toRoomResponseList(roomRepository.findAll());
  }

  @Transactional
  public ExaminationRoomDTO.Response createRoom(ExaminationRoomDTO.Request req) {
    if (roomRepository.existsByRoomCode(req.roomCode())) {
      throw new ResourceAlreadyExistsException(
          "Room with code " + req.roomCode() + " already exists");
    }
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException(
          "Department ID " + req.departmentId() + " not found in HR service");
    }
    ExaminationRoom room = catalogMapper.toRoomEntity(req);
    if (req.modalityId() != null) {
      DiagnosticModality mod =
          modalityRepository
              .findById(req.modalityId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Modality not found: " + req.modalityId()));
      room.setModality(mod);
    }
    return catalogMapper.toRoomResponse(roomRepository.save(room));
  }

  // --- Priority Levels ---
  @Transactional(readOnly = true)
  public List<QueuePriorityLevelDTO.Response> getAllPriorities() {
    return catalogMapper.toPriorityResponseList(priorityRepository.findAll());
  }

  @Transactional
  public QueuePriorityLevelDTO.Response createPriority(QueuePriorityLevelDTO.Request req) {
    if (priorityRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Priority with code " + req.code() + " already exists");
    }
    QueuePriorityLevel p = catalogMapper.toPriorityEntity(req);
    return catalogMapper.toPriorityResponse(priorityRepository.save(p));
  }

  // --- Statuses ---
  @Transactional(readOnly = true)
  public List<ReservationStatusDTO.Response> getAllStatuses() {
    return catalogMapper.toStatusResponseList(statusRepository.findAll());
  }

  @Transactional
  public ReservationStatusDTO.Response createStatus(ReservationStatusDTO.Request req) {
    if (statusRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Status with code " + req.code() + " already exists");
    }
    ReservationStatus s = catalogMapper.toStatusEntity(req);
    return catalogMapper.toStatusResponse(statusRepository.save(s));
  }

  // --- Reception Stations ---
  @Transactional(readOnly = true)
  public List<ReceptionStationDTO.Response> getAllStations() {
    return catalogMapper.toStationResponseList(stationRepository.findAll());
  }

  @Transactional
  public ReceptionStationDTO.Response createStation(ReceptionStationDTO.Request req) {
    if (stationRepository.existsByStationCode(req.stationCode())) {
      throw new ResourceAlreadyExistsException(
          "Station code " + req.stationCode() + " already exists");
    }
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException(
          "Department ID " + req.departmentId() + " not found in HR service");
    }
    ReservationCategory cat =
        categoryRepository
            .findById(req.categoryId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Category not found: " + req.categoryId()));

    ReceptionStation station = catalogMapper.toStationEntity(req);
    station.setCategory(cat);
    return catalogMapper.toStationResponse(stationRepository.save(station));
  }
}
