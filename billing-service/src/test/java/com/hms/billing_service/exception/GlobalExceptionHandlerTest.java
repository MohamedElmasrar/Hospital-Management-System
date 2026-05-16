package com.hms.billing_service.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handleNotFound_returns404() {
    CashDeskNotFoundException ex = new CashDeskNotFoundException("Cash desk not found");
    ResponseEntity<Map<String, String>> response = handler.handleNotFoundExceptions(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("error", "Cash desk not found");
  }

  @Test
  void handleResourceAlreadyExists_returns400() {
    ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Already exists");
    ResponseEntity<Map<String, String>> response = handler.handleResourceAlreadyExists(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("error", "Already exists");
  }

  @Test
  void handleInvalidOperation_returns400() {
    InvalidOperationException ex = new InvalidOperationException("Invalid state transition");
    ResponseEntity<Map<String, String>> response = handler.handleInvalidOperation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("error", "Invalid state transition");
  }

  @Test
  void handleUnauthorizedOperation_returns403() {
    UnauthorizedOperationException ex = new UnauthorizedOperationException("Cannot perform action");
    ResponseEntity<Map<String, String>> response = handler.handleUnauthorizedOperation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("error", "Cannot perform action");
  }

  @Test
  void handleAccessDenied_returns403() {
    AccessDeniedException ex = new AccessDeniedException("Access Denied");
    ResponseEntity<Map<String, String>> response = handler.handleAccessDenied(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("error", "Access Denied");
  }
}
