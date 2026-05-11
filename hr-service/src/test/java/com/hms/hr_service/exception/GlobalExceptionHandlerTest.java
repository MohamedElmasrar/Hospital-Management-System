package com.hms.hr_service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handleUnauthorizedOperation_returnsForbiddenWithErrorMessage() {
    UnauthorizedOperationException ex =
        new UnauthorizedOperationException("Custom unauthorized message");
    ResponseEntity<Map<String, String>> response = handler.handleUnauthorizedOperation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("error", "Custom unauthorized message");
  }

  @Test
  void handleUnauthorizedOperation_defaultConstructor_returnsForbiddenWithDefaultMessage() {
    UnauthorizedOperationException ex = new UnauthorizedOperationException();
    ResponseEntity<Map<String, String>> response = handler.handleUnauthorizedOperation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("error", "Unauthorized operation");
  }

  @Test
  void handleUnauthorizedOpration_subclass_returnsForbidden() {
    UnauthorizedOprationException ex = new UnauthorizedOprationException("Subclass error");
    ResponseEntity<Map<String, String>> response = handler.handleUnauthorizedOperation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("error", "Subclass error");
  }
}
