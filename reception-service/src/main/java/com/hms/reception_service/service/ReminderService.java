package com.hms.reception_service.service;

import com.hms.reception_service.dto.reminder.ReminderDTO;
import com.hms.reception_service.exception.ResourceNotFoundException;
import com.hms.reception_service.model.AppointmentReminder;
import com.hms.reception_service.repository.AppointmentReminderRepository;
import com.hms.reception_service.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReminderService {
  private final AppointmentReminderRepository reminderRepository;
  private final ReservationRepository reservationRepository;

  @Transactional
  public ReminderDTO.Response create(ReminderDTO.CreateRequest request) {
    AppointmentReminder reminder = new AppointmentReminder();
    reminder.setReservation(
        reservationRepository
            .findById(request.reservationId())
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found")));
    reminder.setChannel(request.channel().toUpperCase());
    reminder.setRecipient(request.recipient());
    reminder.setScheduledFor(request.scheduledFor());
    return toResponse(reminderRepository.save(reminder));
  }

  @Transactional(readOnly = true)
  public List<ReminderDTO.Response> findByReservation(UUID reservationId) {
    return reminderRepository.findByReservationId(reservationId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public ReminderDTO.Response markSent(UUID id) {
    AppointmentReminder reminder =
        reminderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
    reminder.setSentAt(LocalDateTime.now());
    return toResponse(reminderRepository.save(reminder));
  }

  private ReminderDTO.Response toResponse(AppointmentReminder reminder) {
    return new ReminderDTO.Response(
        reminder.getId(),
        reminder.getReservation().getId(),
        reminder.getChannel(),
        reminder.getRecipient(),
        reminder.getScheduledFor(),
        reminder.getSentAt());
  }
}
