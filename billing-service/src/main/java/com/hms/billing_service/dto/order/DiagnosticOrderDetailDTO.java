package com.hms.billing_service.dto.order;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record DiagnosticOrderDetailDTO(
    @NotBlank @Length(max = 50) String modality,
    @NotBlank @Length(max = 100) String bodyPart,
    Boolean contrastUsed) {}

