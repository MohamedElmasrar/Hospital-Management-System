package com.hms.hr_service.validation.validator;

import com.hms.hr_service.validation.AllowedSortFields;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

public class AllowedSortFieldsValidator
    implements ConstraintValidator<AllowedSortFields, Pageable> {

  private Set<String> allowedFields;

  @Override
  public void initialize(AllowedSortFields annotation) {
    allowedFields = Arrays.stream(annotation.value()).collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(Pageable pageable, ConstraintValidatorContext context) {
    if (pageable == null || !pageable.getSort().isSorted()) {
      return true;
    }

    return pageable.getSort().stream()
        .allMatch(order -> allowedFields.contains(order.getProperty()));
  }
}
