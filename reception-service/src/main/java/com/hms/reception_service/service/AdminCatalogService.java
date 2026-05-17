package com.hms.reception_service.service;

import com.hms.reception_service.dto.catalog.*;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceAlreadyExistsException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.model.*;
import com.hms.reception_service.repository.*;
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

  // --- Categories ---
  @Transactional(readOnly = true)
  public List<ReservationCategoryDTO.Response> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::mapCategoryToDTO).toList();
  }

  @Transactional
  public ReservationCategoryDTO.Response createCategory(ReservationCategoryDTO.Request req) {
    if (categoryRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Category with code " + req.code() + " already exists");
    }
    ReservationCategory cat = new ReservationCategory();
    cat.setCode(req.code());
    cat.setName(req.name());
    cat.setDescription(req.description());
    cat.setRequiresPhysician(
        req.requiresPhysician() != null ? req.requiresPhysician() : Boolean.TRUE);
    cat.setRequiresPrescription(
        req.requiresPrescription() != null ? req.requiresPrescription() : Boolean.FALSE);
    cat.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapCategoryToDTO(categoryRepository.save(cat));
  }

  // --- Modalities ---
  @Transactional(readOnly = true)
  public List<DiagnosticModalityDTO.Response> getAllModalities() {
    return modalityRepository.findAll().stream().map(this::mapModalityToDTO).toList();
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
    DiagnosticModality mod = new DiagnosticModality();
    mod.setCode(req.code());
    mod.setName(req.name());
    mod.setDepartmentId(req.departmentId());
    mod.setDefaultDurationMinutes(
        req.defaultDurationMinutes() != null ? req.defaultDurationMinutes() : 30);
    mod.setRequiresContrastScreening(
        req.requiresContrastScreening() != null ? req.requiresContrastScreening() : Boolean.FALSE);
    mod.setRequiresSafetyChecklist(
        req.requiresSafetyChecklist() != null ? req.requiresSafetyChecklist() : Boolean.FALSE);
    mod.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapModalityToDTO(modalityRepository.save(mod));
  }

  // --- Examination Rooms ---
  @Transactional(readOnly = true)
  public List<ExaminationRoomDTO.Response> getAllRooms() {
    return roomRepository.findAll().stream().map(this::mapRoomToDTO).toList();
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
    ExaminationRoom room = new ExaminationRoom();
    room.setRoomCode(req.roomCode());
    room.setName(req.name());
    room.setDepartmentId(req.departmentId());
    if (req.modalityId() != null) {
      DiagnosticModality mod =
          modalityRepository
              .findById(req.modalityId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Modality not found: " + req.modalityId()));
      room.setModality(mod);
    }
    room.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapRoomToDTO(roomRepository.save(room));
  }

  // --- Priority Levels ---
  @Transactional(readOnly = true)
  public List<QueuePriorityLevelDTO.Response> getAllPriorities() {
    return priorityRepository.findAll().stream().map(this::mapPriorityToDTO).toList();
  }

  @Transactional
  public QueuePriorityLevelDTO.Response createPriority(QueuePriorityLevelDTO.Request req) {
    if (priorityRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Priority with code " + req.code() + " already exists");
    }
    QueuePriorityLevel p = new QueuePriorityLevel();
    p.setCode(req.code());
    p.setName(req.name());
    p.setWeight(req.weight());
    if (req.badgeColorHex() != null) p.setBadgeColorHex(req.badgeColorHex());
    p.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapPriorityToDTO(priorityRepository.save(p));
  }

  // --- Statuses ---
  @Transactional(readOnly = true)
  public List<ReservationStatusDTO.Response> getAllStatuses() {
    return statusRepository.findAll().stream().map(this::mapStatusToDTO).toList();
  }

  @Transactional
  public ReservationStatusDTO.Response createStatus(ReservationStatusDTO.Request req) {
    if (statusRepository.existsByCode(req.code())) {
      throw new ResourceAlreadyExistsException(
          "Status with code " + req.code() + " already exists");
    }
    ReservationStatus s = new ReservationStatus();
    s.setCode(req.code());
    s.setName(req.name());
    if (req.badgeColorHex() != null) s.setBadgeColorHex(req.badgeColorHex());
    s.setAllowsQueueing(req.allowsQueueing() != null ? req.allowsQueueing() : Boolean.FALSE);
    s.setIsTerminal(req.isTerminal() != null ? req.isTerminal() : Boolean.FALSE);
    s.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapStatusToDTO(statusRepository.save(s));
  }

  // --- Reception Stations ---
  @Transactional(readOnly = true)
  public List<ReceptionStationDTO.Response> getAllStations() {
    return stationRepository.findAll().stream().map(this::mapStationToDTO).toList();
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

    ReceptionStation station = new ReceptionStation();
    station.setStationCode(req.stationCode());
    station.setName(req.name());
    station.setDepartmentId(req.departmentId());
    station.setCategory(cat);
    station.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);
    return mapStationToDTO(stationRepository.save(station));
  }

  // --- Mappers ---
  private ReservationCategoryDTO.Response mapCategoryToDTO(ReservationCategory c) {
    return new ReservationCategoryDTO.Response(
        c.getId(),
        c.getCode(),
        c.getName(),
        c.getDescription(),
        c.getRequiresPhysician(),
        c.getRequiresPrescription(),
        c.getIsActive(),
        c.getDateCreated());
  }

  private DiagnosticModalityDTO.Response mapModalityToDTO(DiagnosticModality m) {
    return new DiagnosticModalityDTO.Response(
        m.getId(),
        m.getCode(),
        m.getName(),
        m.getDepartmentId(),
        m.getDefaultDurationMinutes(),
        m.getRequiresContrastScreening(),
        m.getRequiresSafetyChecklist(),
        m.getIsActive(),
        m.getDateCreated());
  }

  private ExaminationRoomDTO.Response mapRoomToDTO(ExaminationRoom r) {
    return new ExaminationRoomDTO.Response(
        r.getId(),
        r.getRoomCode(),
        r.getName(),
        r.getDepartmentId(),
        r.getModality() != null ? r.getModality().getId() : null,
        r.getModality() != null ? r.getModality().getName() : null,
        r.getIsActive(),
        r.getDateCreated());
  }

  private QueuePriorityLevelDTO.Response mapPriorityToDTO(QueuePriorityLevel p) {
    return new QueuePriorityLevelDTO.Response(
        p.getId(), p.getCode(), p.getName(), p.getWeight(), p.getBadgeColorHex(), p.getIsActive());
  }

  private ReservationStatusDTO.Response mapStatusToDTO(ReservationStatus s) {
    return new ReservationStatusDTO.Response(
        s.getId(),
        s.getCode(),
        s.getName(),
        s.getBadgeColorHex(),
        s.getAllowsQueueing(),
        s.getIsTerminal(),
        s.getIsActive());
  }

  private ReceptionStationDTO.Response mapStationToDTO(ReceptionStation st) {
    return new ReceptionStationDTO.Response(
        st.getId(),
        st.getStationCode(),
        st.getName(),
        st.getDepartmentId(),
        st.getCategory().getId(),
        st.getCategory().getName(),
        st.getIsActive());
  }
}
