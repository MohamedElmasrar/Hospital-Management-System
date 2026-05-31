package com.hms.reception_service.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.hms.reception_service.dto.catalog.*;
import com.hms.reception_service.dto.patient.PatientDTO;
import com.hms.reception_service.dto.prescription.DiagnosticPrescriptionDTO;
import com.hms.reception_service.dto.queue.QueueDTO;
import com.hms.reception_service.dto.reservation.ReservationDTO;
import com.hms.reception_service.dto.schedule.ScheduleDTO;
import com.hms.reception_service.model.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ReceptionMappersTest {

  private final CatalogMapper catalogMapper = Mappers.getMapper(CatalogMapper.class);
  private final PatientMapper patientMapper = Mappers.getMapper(PatientMapper.class);
  private final ScheduleMapper scheduleMapper = Mappers.getMapper(ScheduleMapper.class);
  private final DiagnosticPrescriptionMapper prescriptionMapper =
      Mappers.getMapper(DiagnosticPrescriptionMapper.class);
  private final ReservationMapper reservationMapper = Mappers.getMapper(ReservationMapper.class);
  private final QueueMapper queueMapper = Mappers.getMapper(QueueMapper.class);

  @Test
  void testCatalogMapper_Category() {
    ReservationCategory cat = new ReservationCategory();
    cat.setId(UUID.randomUUID());
    cat.setCode("CONSULTATION");
    cat.setName("Doctor Consultation");
    cat.setDescription("General consult");
    cat.setRequiresPhysician(true);
    cat.setRequiresPrescription(false);
    cat.setIsActive(true);
    cat.setDateCreated(new Date());

    ReservationCategoryDTO.Response response = catalogMapper.toCategoryResponse(cat);
    assertEquals(cat.getId(), response.id());
    assertEquals("CONSULTATION", response.code());
    assertEquals("Doctor Consultation", response.name());
    assertTrue(response.requiresPhysician());

    ReservationCategoryDTO.Request req =
        new ReservationCategoryDTO.Request("LAB", "Laboratory", "Blood tests", false, true, true);
    ReservationCategory entity = catalogMapper.toCategoryEntity(req);
    assertEquals("LAB", entity.getCode());
    assertEquals("Laboratory", entity.getName());
    assertFalse(entity.getRequiresPhysician());
    assertTrue(entity.getRequiresPrescription());
  }

  @Test
  void testCatalogMapper_ModalityAndRoom() {
    DiagnosticModality mod = new DiagnosticModality();
    mod.setId(UUID.randomUUID());
    mod.setCode("MRI");
    mod.setName("Magnetic Resonance Imaging");
    mod.setDepartmentId(UUID.randomUUID());
    mod.setDefaultDurationMinutes(45);
    mod.setRequiresContrastScreening(true);
    mod.setRequiresSafetyChecklist(true);
    mod.setIsActive(true);

    DiagnosticModalityDTO.Response modResp = catalogMapper.toModalityResponse(mod);
    assertEquals("MRI", modResp.code());
    assertEquals(45, modResp.defaultDurationMinutes());

    ExaminationRoom room = new ExaminationRoom();
    room.setId(UUID.randomUUID());
    room.setRoomCode("MRI-01");
    room.setName("MRI Scanner Bay 1");
    room.setDepartmentId(mod.getDepartmentId());
    room.setModality(mod);
    room.setIsActive(true);

    ExaminationRoomDTO.Response roomResp = catalogMapper.toRoomResponse(room);
    assertEquals("MRI-01", roomResp.roomCode());
    assertEquals("MRI Scanner Bay 1", roomResp.name());
    assertEquals(mod.getId(), roomResp.modalityId());
    assertEquals("Magnetic Resonance Imaging", roomResp.modalityName());
  }

  @Test
  void testPatientMapper() {
    PatientDTO.CreateRequest req =
        new PatientDTO.CreateRequest(
            "CIN12345",
            "Sarah",
            "Connor",
            LocalDate.of(1985, 3, 15),
            "FEMALE",
            "A_POSITIVE",
            "+1234567890",
            "+0987654321",
            "John Connor",
            "Penicillin",
            "None");

    Patient patient = patientMapper.toEntity(req);
    assertEquals("Sarah", patient.getFirstName());
    assertEquals("Connor", patient.getLastName());
    assertEquals("CIN12345", patient.getNationalId());
    assertEquals("A_POSITIVE", patient.getBloodGroup());
    assertTrue(patient.getIsActive());

    patient.setId(UUID.randomUUID());
    patient.setMrn("MRN-20261017-1001");
    patient.setDateCreated(new Date());
    patient.setDateModified(new Date());

    PatientDTO.Response resp = patientMapper.toResponse(patient);
    assertEquals(patient.getId(), resp.id());
    assertEquals("MRN-20261017-1001", resp.mrn());
    assertEquals("Sarah", resp.firstName());
    assertEquals("Connor", resp.lastName());

    PatientDTO.UpdateRequest updateReq =
        new PatientDTO.UpdateRequest(
            null,
            "Sarah Jane",
            null,
            null,
            null,
            null,
            "+1112223334",
            null,
            null,
            null,
            null,
            null);
    patientMapper.updatePatientFromDTO(updateReq, patient);
    assertEquals("Sarah Jane", patient.getFirstName());
    assertEquals("Connor", patient.getLastName()); // unchanged
    assertEquals("+1112223334", patient.getPhone());
  }

  @Test
  void testScheduleMapper() {
    ExaminationRoom room = new ExaminationRoom();
    room.setId(UUID.randomUUID());
    room.setName("Exam Room 3B");

    ServiceSchedule schedule = new ServiceSchedule();
    schedule.setId(UUID.randomUUID());
    schedule.setDepartmentId(UUID.randomUUID());
    schedule.setPractitionerId(UUID.randomUUID());
    schedule.setRoom(room);
    schedule.setDayOfWeek(DayOfWeek.MONDAY);
    schedule.setStartTime(LocalTime.of(9, 0));
    schedule.setEndTime(LocalTime.of(17, 0));
    schedule.setSlotDurationMinutes(20);
    schedule.setIsActive(true);

    ScheduleDTO.Response resp = scheduleMapper.toResponse(schedule);
    assertEquals(schedule.getId(), resp.id());
    assertEquals(room.getId(), resp.roomId());
    assertEquals("Exam Room 3B", resp.roomName());
    assertEquals(DayOfWeek.MONDAY, resp.dayOfWeek());
    assertEquals(20, resp.slotDurationMinutes());
  }

  @Test
  void testDiagnosticPrescriptionMapper() {
    Patient patient = new Patient();
    patient.setId(UUID.randomUUID());
    patient.setFirstName("Alice");
    patient.setLastName("Smith");

    DiagnosticModality modality = new DiagnosticModality();
    modality.setId(UUID.randomUUID());
    modality.setCode("XRAY");
    modality.setName("X-Ray Digital");

    DiagnosticPrescription rx = new DiagnosticPrescription();
    rx.setId(UUID.randomUUID());
    rx.setPrescriptionCode("RX-20261017-9999");
    rx.setPatient(patient);
    rx.setReferringPractitionerId(UUID.randomUUID());
    rx.setModality(modality);
    rx.setBodyPart("Chest");
    rx.setContrastRequired(false);
    rx.setClinicalNotes("Persistent cough");
    rx.setIsFulfilled(false);
    rx.setCreatedAt(new Date());

    DiagnosticPrescriptionDTO.Response resp = prescriptionMapper.toResponse(rx);
    assertEquals(rx.getId(), resp.id());
    assertEquals("RX-20261017-9999", resp.prescriptionCode());
    assertEquals("Alice Smith", resp.patientName());
    assertEquals(patient.getId(), resp.patientId());
    assertEquals(modality.getId(), resp.modalityId());
    assertEquals("XRAY", resp.modalityCode());
    assertEquals("X-Ray Digital", resp.modalityName());
    assertEquals("Chest", resp.bodyPart());
  }

  @Test
  void testReservationMapper() {
    Patient patient = new Patient();
    patient.setId(UUID.randomUUID());
    patient.setFirstName("Bob");
    patient.setLastName("Brown");
    patient.setPhone("+212611223344");

    ReservationCategory cat = new ReservationCategory();
    cat.setId(UUID.randomUUID());
    cat.setCode("DIAGNOSTIC_IMAGING");
    cat.setName("Diagnostic Imaging");

    ReservationStatus status = new ReservationStatus();
    status.setId(UUID.randomUUID());
    status.setCode("PENDING_PAYMENT");
    status.setName("Pending Payment");
    status.setBadgeColorHex("#F59E0B");

    ExaminationRoom room = new ExaminationRoom();
    room.setId(UUID.randomUUID());
    room.setName("MRI Scanner Bay 2");

    DiagnosticModality mod = new DiagnosticModality();
    mod.setId(UUID.randomUUID());
    mod.setCode("MRI");
    mod.setName("MRI Scanner");

    Reservation res = new Reservation();
    res.setId(UUID.randomUUID());
    res.setReservationNumber("MRI-20261017-1234");
    res.setPatient(patient);
    res.setCategory(cat);
    res.setDepartmentId(UUID.randomUUID());
    res.setRoom(room);
    res.setReservationDate(LocalDate.of(2026, 10, 20));
    res.setStartTime(LocalTime.of(10, 0));
    res.setEndTime(LocalTime.of(10, 45));
    res.setStatus(status);
    res.setIsPaid(false);
    res.setReceptionistStaffId(UUID.randomUUID());
    res.setCreatedAt(new Date());
    res.setUpdatedAt(new Date());

    DiagnosticReservationDetail detail = new DiagnosticReservationDetail();
    detail.setReservation(res);
    detail.setModality(mod);
    detail.setBodyPart("Brain");
    detail.setContrastUsed(true);
    detail.setSafetyChecklistCompleted(true);
    res.setDiagnosticDetail(detail);

    ReservationDTO.Response resp = reservationMapper.toResponse(res);
    assertEquals(res.getId(), resp.id());
    assertEquals("MRI-20261017-1234", resp.reservationNumber());
    assertEquals("Bob Brown", resp.patientName());
    assertEquals("+212611223344", resp.patientPhone());
    assertEquals("DIAGNOSTIC_IMAGING", resp.categoryCode());
    assertEquals("PENDING_PAYMENT", resp.statusCode());
    assertEquals("#F59E0B", resp.badgeColorHex());
    assertEquals("MRI Scanner Bay 2", resp.roomName());
    assertNotNull(resp.diagnosticDetail());
    assertEquals("Brain", resp.diagnosticDetail().bodyPart());
    assertTrue(resp.diagnosticDetail().contrastUsed());
    assertEquals("MRI", resp.diagnosticDetail().modalityCode());
  }

  @Test
  void testQueueMapper() {
    ExaminationRoom room = new ExaminationRoom();
    room.setId(UUID.randomUUID());
    room.setName("Room 101");

    DailyQueue queue = new DailyQueue();
    queue.setId(UUID.randomUUID());
    queue.setDepartmentId(UUID.randomUUID());
    queue.setRoom(room);
    queue.setQueueDate(LocalDate.now());
    queue.setCurrentTicketNumber("CONS-005");
    queue.setLastIssuedSequence(5);
    queue.setIsOpen(true);

    QueueDTO.DailyQueueResponse qResp = queueMapper.toDailyQueueResponse(queue);
    assertEquals(queue.getId(), qResp.id());
    assertEquals(room.getId(), qResp.roomId());
    assertEquals("Room 101", qResp.roomName());
    assertEquals("CONS-005", qResp.currentTicketNumber());

    Patient patient = new Patient();
    patient.setId(UUID.randomUUID());
    patient.setFirstName("Charlie");
    patient.setLastName("Chaplin");

    Reservation res = new Reservation();
    res.setId(UUID.randomUUID());
    res.setPatient(patient);
    res.setRoom(room);

    QueuePriorityLevel priority = new QueuePriorityLevel();
    priority.setId(UUID.randomUUID());
    priority.setCode("URGENT");
    priority.setName("Urgent");
    priority.setWeight(75);

    QueueTicket ticket = new QueueTicket();
    ticket.setId(UUID.randomUUID());
    ticket.setQueue(queue);
    ticket.setReservation(res);
    ticket.setTicketCode("CONS-006");
    ticket.setSequenceNumber(6);
    ticket.setPriority(priority);
    ticket.setStatus("WAITING");
    ticket.setIssuedAt(new Date());

    QueueDTO.TicketResponse tResp = queueMapper.toTicketResponse(ticket);
    assertEquals(ticket.getId(), tResp.id());
    assertEquals(queue.getId(), tResp.queueId());
    assertEquals(res.getId(), tResp.reservationId());
    assertEquals("CONS-006", tResp.ticketCode());
    assertEquals(6, tResp.sequenceNumber());
    assertEquals(priority.getId(), tResp.priorityId());
    assertEquals("Urgent", tResp.priorityName());
    assertEquals(75, tResp.priorityWeight());
    assertEquals("Charlie Chaplin", tResp.patientName());
    assertEquals("Room 101", tResp.roomName());
  }
}
