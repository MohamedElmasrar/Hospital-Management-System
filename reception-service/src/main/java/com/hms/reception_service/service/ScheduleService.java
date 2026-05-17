package com.hms.reception_service.service;

import com.hms.reception_service.dto.schedule.ScheduleDTO;
import com.hms.reception_service.exception.InvalidOperationException;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.grpc.HrGrpcClient;
import com.hms.reception_service.model.ExaminationRoom;
import com.hms.reception_service.model.Reservation;
import com.hms.reception_service.model.ServiceSchedule;
import com.hms.reception_service.repository.ExaminationRoomRepository;
import com.hms.reception_service.repository.ReservationRepository;
import com.hms.reception_service.repository.ServiceScheduleRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ServiceScheduleRepository scheduleRepository;
  private final ExaminationRoomRepository roomRepository;
  private final ReservationRepository reservationRepository;
  private final HrGrpcClient hrGrpcClient;

  @Transactional
  public ScheduleDTO.Response createSchedule(ScheduleDTO.CreateRequest req) {
    if (!hrGrpcClient.validateDepartment(req.departmentId())) {
      throw new InvalidOperationException("Department ID not found in HR: " + req.departmentId());
    }
    if (req.practitionerId() != null && !hrGrpcClient.validatePhysician(req.practitionerId())) {
      throw new InvalidOperationException("Physician ID not found in HR: " + req.practitionerId());
    }
    ExaminationRoom room =
        roomRepository
            .findById(req.roomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + req.roomId()));

    if (!req.endTime().isAfter(req.startTime())) {
      throw new InvalidOperationException("End time must be after start time");
    }

    ServiceSchedule schedule = new ServiceSchedule();
    schedule.setDepartmentId(req.departmentId());
    schedule.setPractitionerId(req.practitionerId());
    schedule.setRoom(room);
    schedule.setDayOfWeek(req.dayOfWeek());
    schedule.setStartTime(req.startTime());
    schedule.setEndTime(req.endTime());
    schedule.setSlotDurationMinutes(
        req.slotDurationMinutes() != null ? req.slotDurationMinutes() : 30);
    schedule.setIsActive(req.isActive() != null ? req.isActive() : Boolean.TRUE);

    return mapToDTO(scheduleRepository.save(schedule));
  }

  @Transactional(readOnly = true)
  public List<ScheduleDTO.Response> getSchedulesByDepartment(UUID departmentId) {
    return scheduleRepository.findByDepartmentIdAndIsActiveTrue(departmentId).stream()
        .map(this::mapToDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ScheduleDTO.Response> getSchedulesByPractitioner(UUID practitionerId) {
    return scheduleRepository.findByPractitionerIdAndIsActiveTrue(practitionerId).stream()
        .map(this::mapToDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ScheduleDTO.AvailableSlot> getAvailableSlots(
      UUID practitionerId, UUID roomId, LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    List<ServiceSchedule> schedules;

    if (practitionerId != null) {
      schedules =
          scheduleRepository.findByPractitionerIdAndDayOfWeekAndIsActiveTrue(
              practitionerId, dayOfWeek);
    } else if (roomId != null) {
      schedules = scheduleRepository.findByRoomIdAndDayOfWeekAndIsActiveTrue(roomId, dayOfWeek);
    } else {
      throw new InvalidOperationException(
          "Either practitionerId or roomId must be provided to query slots");
    }

    List<ScheduleDTO.AvailableSlot> availableSlots = new ArrayList<>();

    for (ServiceSchedule schedule : schedules) {
      LocalTime current = schedule.getStartTime();
      int duration = schedule.getSlotDurationMinutes();

      while (current.plusMinutes(duration).isBefore(schedule.getEndTime())
          || current.plusMinutes(duration).equals(schedule.getEndTime())) {
        LocalTime slotEnd = current.plusMinutes(duration);

        boolean isConflicted;
        if (practitionerId != null) {
          List<Reservation> conflicts =
              reservationRepository.findConflictingPractitionerReservations(
                  practitionerId, date, current, slotEnd);
          isConflicted = !conflicts.isEmpty();
        } else {
          List<Reservation> conflicts =
              reservationRepository.findConflictingRoomReservations(
                  schedule.getRoom().getId(), date, current, slotEnd);
          isConflicted = !conflicts.isEmpty();
        }

        availableSlots.add(
            new ScheduleDTO.AvailableSlot(
                date,
                current,
                slotEnd,
                schedule.getRoom().getId(),
                schedule.getRoom().getName(),
                schedule.getPractitionerId(),
                !isConflicted));

        current = slotEnd;
      }
    }

    return availableSlots;
  }

  private ScheduleDTO.Response mapToDTO(ServiceSchedule s) {
    return new ScheduleDTO.Response(
        s.getId(),
        s.getDepartmentId(),
        s.getPractitionerId(),
        s.getRoom().getId(),
        s.getRoom().getName(),
        s.getDayOfWeek(),
        s.getStartTime(),
        s.getEndTime(),
        s.getSlotDurationMinutes(),
        s.getIsActive());
  }
}
