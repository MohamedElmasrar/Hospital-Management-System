package com.hms.auth_service.mapper;

import com.hms.auth_service.dto.admin.UserResponse;
import com.hms.auth_service.dto.register.RegisterRequestDTO;
import com.hms.auth_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "role", constant = "EMPLOYEE")
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "email", source = "request.email")
  @Mapping(target = "passwordHash", source = "passwordHash")
  User toUser(RegisterRequestDTO request, String passwordHash);

  UserResponse toResponse(User user);
}
