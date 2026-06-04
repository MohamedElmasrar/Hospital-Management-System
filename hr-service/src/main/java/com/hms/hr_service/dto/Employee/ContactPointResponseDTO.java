package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.ContactType;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ContactPointResponseDTO(
    UUID id, ContactType contactType, String telephone, String email, boolean isPreferred) {}
