package com.hms.auth_service.service;

import com.hms.auth_service.dto.admin.UserResponse;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.enums.UserRole;
import com.hms.auth_service.exception.UserWithTheSameEmailAlreadyExistsException;
import com.hms.auth_service.mapper.UserMapper;
import com.hms.auth_service.messaging.AnalyticsEventPublisher;
import com.hms.auth_service.messaging.NotificationEventPublisher;
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
  private final AnalyticsEventPublisher analyticsEventPublisher;
  private final NotificationEventPublisher notificationEventPublisher;

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public List<UserResponse> findAll() {
    return userRepository.findAll().stream().map(user -> userMapper.toResponse(user)).toList();
  }

  public UserResponse findById(UUID id) {
    User user = userRepository.findById(id).orElseThrow();
    return userMapper.toResponse(user);
  }

  @Transactional
  public User updateRole(UUID id, UserRole role) {
    if (role == UserRole.DISABLED) {
      throw new IllegalArgumentException("Disabled is not an assignable role");
    }
    User user = userRepository.findById(id).orElseThrow();
    if (user.getRole() == role) {
      return user;
    }
    user.setRole(role);
    User saved = userRepository.save(user);
    analyticsEventPublisher.publishUserEvent("USER_ROLE_UPDATED", saved.getId(), saved.getRole());
    notificationEventPublisher.publish(
        saved.getId(), "Account role updated", "Your account role is now " + saved.getRole() + ".");
    return saved;
  }

  @Transactional
  public User updateActive(UUID id, boolean active) {
    User user = userRepository.findById(id).orElseThrow();
    if (user.isActive() == active) {
      return user;
    }
    user.setActive(active);
    User saved = userRepository.save(user);
    analyticsEventPublisher.publishUserEvent(
        active ? "USER_ACTIVATED" : "USER_DEACTIVATED", saved.getId(), saved.getRole());
    notificationEventPublisher.publish(
        saved.getId(),
        "Account status updated",
        active ? "Your account has been activated." : "Your account has been deactivated.");
    return saved;
  }

  @Transactional
  public void registerNewUser(RegisterRequestDTO request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new UserWithTheSameEmailAlreadyExistsException();
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User userToSave = userMapper.toUser(request, passwordHash);

    User saved = userRepository.save(userToSave);
    analyticsEventPublisher.publishUserEvent("USER_REGISTERED", saved.getId(), saved.getRole());
    notificationEventPublisher.publish(
        saved.getId(), "Welcome", "Your hospital account has been created.");
  }
}
