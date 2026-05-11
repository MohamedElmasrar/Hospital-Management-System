package com.hms.hr_service.repository;

import com.hms.hr_service.model.PostalAddress;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostalAddressRepository extends JpaRepository<PostalAddress, UUID> {

  Optional<PostalAddress> findByEmployeeId(UUID employeeId);
}
