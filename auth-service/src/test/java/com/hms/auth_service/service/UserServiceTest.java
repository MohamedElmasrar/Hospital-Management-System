package com.hms.auth_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.exception.UserWithTheSameEmailAlreadyExistsException;
import com.hms.auth_service.mapper.UserMapper;
import com.hms.auth_service.model.User;
import com.hms.auth_service.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private UserMapper userMapper;

  @InjectMocks private UserService userService;

  @Test
  void findByEmail_whenUserExists_returnsUserOptional() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("user@example.com");

    given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

    Optional<User> result = userService.findByEmail("user@example.com");

    assertThat(result).isPresent().contains(user);
    verify(userRepository).findByEmail("user@example.com");
  }

  @Test
  void findByEmail_whenUserDoesNotExist_returnsEmptyOptional() {
    given(userRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());

    Optional<User> result = userService.findByEmail("nonexistent@example.com");

    assertThat(result).isEmpty();
    verify(userRepository).findByEmail("nonexistent@example.com");
  }

  @Test
  void registerNewUser_validRequest_encodesPasswordAndSavesUser() {
    RegisterRequestDTO request = new RegisterRequestDTO("new_user@email.com", "plainPassword123");
    User expectedUser = new User();
    expectedUser.setEmail("new_user@email.com");
    expectedUser.setPasswordHash("$2a$10$encodedPasswordHash");

    given(userRepository.existsByEmail("new_user@email.com")).willReturn(false);
    given(passwordEncoder.encode("plainPassword123")).willReturn("$2a$10$encodedPasswordHash");
    given(userMapper.toUser(request, "$2a$10$encodedPasswordHash")).willReturn(expectedUser);

    userService.registerNewUser(request);

    verify(userRepository).save(expectedUser);
  }

  @Test
  void registerNewUser_emailAlreadyExists_throwsUserWithTheSameEmailAlreadyExistsException() {
    RegisterRequestDTO request = new RegisterRequestDTO("existing@email.com", "plainPassword123");

    given(userRepository.existsByEmail("existing@email.com")).willReturn(true);

    assertThatThrownBy(() -> userService.registerNewUser(request))
        .isInstanceOf(UserWithTheSameEmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
    verifyNoInteractions(passwordEncoder);
  }
}
