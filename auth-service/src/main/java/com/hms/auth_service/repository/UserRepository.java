package com.hms.auth_service.repository;

import com.hms.auth_service.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  public Optional<User> findByEmail(String email);
}
