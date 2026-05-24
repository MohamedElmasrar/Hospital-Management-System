package com.hms.reception_service.repository;

import com.hms.reception_service.model.EmergencyAdmission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyAdmissionRepository extends JpaRepository<EmergencyAdmission, UUID> {}
