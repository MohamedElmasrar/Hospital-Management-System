package com.hms.reception_service.config;

import com.hms.reception_service.model.QueuePriorityLevel;
import com.hms.reception_service.model.ReservationCategory;
import com.hms.reception_service.model.ReservationStatus;
import com.hms.reception_service.repository.QueuePriorityLevelRepository;
import com.hms.reception_service.repository.ReservationCategoryRepository;
import com.hms.reception_service.repository.ReservationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogDataInitializer implements CommandLineRunner {

  private final ReservationCategoryRepository categoryRepository;
  private final ReservationStatusRepository statusRepository;
  private final QueuePriorityLevelRepository priorityRepository;

  @Override
  public void run(String... args) {
    seedCategories();
    seedStatuses();
    seedPriorities();
  }

  private void seedCategories() {
    if (categoryRepository.count() == 0) {
      log.info("Seeding default reservation categories...");
      createCategory("CONSULTATION", "Physician Consultation", "Doctor visit", true, false);
      createCategory(
          "DIAGNOSTIC_IMAGING",
          "Diagnostic & Medical Imaging",
          "MRI, CT, X-Ray scans",
          false,
          true);
      createCategory("LABORATORY", "Laboratory Analysis", "Blood & specimen tests", false, true);
    }
  }

  private void createCategory(
      String code,
      String name,
      String description,
      boolean requiresPhysician,
      boolean requiresPrescription) {
    ReservationCategory category = new ReservationCategory();
    category.setCode(code);
    category.setName(name);
    category.setDescription(description);
    category.setRequiresPhysician(requiresPhysician);
    category.setRequiresPrescription(requiresPrescription);
    category.setIsActive(true);
    categoryRepository.save(category);
  }

  private void seedStatuses() {
    if (statusRepository.count() == 0) {
      log.info("Seeding default reservation statuses...");
      createStatus("PENDING_PAYMENT", "Pending Payment", "#F59E0B", false, false);
      createStatus("PAID", "Paid / Ready for Queue", "#3B82F6", true, false);
      createStatus("CHECKED_IN", "Waiting in Lobby", "#8B5CF6", true, false);
      createStatus("CALLED", "Called to Room", "#EC4899", false, false);
      createStatus("IN_SERVICE", "In Examination", "#10B981", false, false);
      createStatus("COMPLETED", "Completed", "#059669", false, true);
      createStatus("CANCELLED", "Cancelled", "#EF4444", false, true);
      createStatus("NO_SHOW", "No Show", "#6B7280", false, true);
    }
  }

  private void createStatus(
      String code, String name, String colorHex, boolean allowsQueueing, boolean isTerminal) {
    ReservationStatus status = new ReservationStatus();
    status.setCode(code);
    status.setName(name);
    status.setBadgeColorHex(colorHex);
    status.setAllowsQueueing(allowsQueueing);
    status.setIsTerminal(isTerminal);
    status.setIsActive(true);
    statusRepository.save(status);
  }

  private void seedPriorities() {
    if (priorityRepository.count() == 0) {
      log.info("Seeding default queue priority levels...");
      createPriority("NORMAL", "Standard Queue", 10, "#10B981");
      createPriority("PRIORITY", "Priority / Senior / Child", 30, "#F59E0B");
      createPriority("URGENT", "Urgent Clinical Need", 60, "#F97316");
      createPriority("EMERGENCY", "Immediate Emergency", 100, "#EF4444");
    }
  }

  private void createPriority(String code, String name, int weight, String colorHex) {
    QueuePriorityLevel priority = new QueuePriorityLevel();
    priority.setCode(code);
    priority.setName(name);
    priority.setWeight(weight);
    priority.setBadgeColorHex(colorHex);
    priority.setIsActive(true);
    priorityRepository.save(priority);
  }
}
