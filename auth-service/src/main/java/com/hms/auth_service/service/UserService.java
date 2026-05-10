package com.hms.auth_service.service;

import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.exception.UserWithTheSameEmailAlreadyExistsException;
import com.hms.auth_service.model.User;
import com.hms.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional
  public void registerNewUser(RegisterRequestDTO request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new UserWithTheSameEmailAlreadyExistsException();
    }

    User userToSave = new User();
    userToSave.setEmail(request.email());
    String passwordHash = passwordEncoder.encode(request.password());
    userToSave.setPasswordHash(passwordHash);

    userRepository.save(userToSave);
  }
}
