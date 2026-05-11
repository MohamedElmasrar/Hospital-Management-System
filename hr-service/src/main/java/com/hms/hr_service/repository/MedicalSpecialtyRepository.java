package com.hms.hr_service.repository;

import com.hms.hr_service.model.MedicalSpecialty;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalSpecialtyRepository extends JpaRepository<MedicalSpecialty, UUID> {

  Optional<MedicalSpecialty> findByName(String name);

  boolean existsByName(String name);
}
