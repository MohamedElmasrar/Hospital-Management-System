package com.hms.auth_service.service;

import com.hms.auth_service.dto.admin.UserResponse;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.enums.UserRole;
import com.hms.auth_service.exception.UserWithTheSameEmailAlreadyExistsException;
import com.hms.auth_service.mapper.UserMapper;
import com.hms.auth_service.model.User;
import com.hms.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public List<UserResponse> findAll() {
    return userRepository.findAll().stream()
        .map(
            user ->
                new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isActive()))
        .toList();
  }

  @Transactional
  public User updateRole(UUID id, UserRole role) {
    User user = userRepository.findById(id).orElseThrow();
    user.setRole(role);
    return userRepository.save(user);
  }

  @Transactional
  public User updateActive(UUID id, boolean active) {
    User user = userRepository.findById(id).orElseThrow();
    user.setActive(active);
    return userRepository.save(user);
  }

  @Transactional
  public void registerNewUser(RegisterRequestDTO request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new UserWithTheSameEmailAlreadyExistsException();
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User userToSave = userMapper.toUser(request, passwordHash);

    userRepository.save(userToSave);
  }
}
