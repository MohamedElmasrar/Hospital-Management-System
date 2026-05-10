package com.hms.auth_service.repository;

import com.hms.auth_service.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  @EntityGraph(attributePaths = {"user", "user.email", "user.password_hash"})
  Optional<RefreshToken> findWithUserByTokenHash(String tokenHash);

  Optional<RefreshToken> findByTokenHash(String hashToken);
}
