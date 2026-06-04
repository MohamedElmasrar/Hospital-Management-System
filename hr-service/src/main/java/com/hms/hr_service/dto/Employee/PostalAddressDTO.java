package com.hms.hr_service.dto.Employee;

import com.hms.hr_service.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record PostalAddressDTO(
    UUID id,
    @NotNull AddressType addressType,
    @NotBlank @Length(max = 255) String streetAddress,
    @NotBlank @Length(max = 100) String addressLocality,
    @NotBlank @Length(max = 100) String addressRegion,
    @NotBlank @Length(max = 20) String postalCode,
    @NotBlank @Length(max = 50) String addressCountry) {}
