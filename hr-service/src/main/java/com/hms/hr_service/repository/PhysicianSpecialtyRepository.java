package com.hms.hr_service.repository;

import com.hms.hr_service.model.PhysicianSpecialty;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysicianSpecialtyRepository extends JpaRepository<PhysicianSpecialty, UUID> {

  List<PhysicianSpecialty> findByPhysicianId(UUID physicianId);

  Optional<PhysicianSpecialty> findByPhysicianIdAndMedicalSpecialtyId(
      UUID physicianId, UUID medicalSpecialtyId);

  void deleteByPhysicianIdAndMedicalSpecialtyId(UUID physicianId, UUID medicalSpecialtyId);
}
