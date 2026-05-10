package com.hms.hr_service.validation;

import com.hms.hr_service.validation.validator.AllowedSortFieldsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedSortFieldsValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedSortFields {

  String[] value();

  String message() default "Invalid sort field";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
