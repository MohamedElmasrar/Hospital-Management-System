package com.hms.hr_service.repository;

import com.hms.hr_service.model.MedicalOrganization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicalOrganizationRepository
    extends JpaRepository<MedicalOrganization, UUID>,
        JpaSpecificationExecutor<MedicalOrganization> {}
