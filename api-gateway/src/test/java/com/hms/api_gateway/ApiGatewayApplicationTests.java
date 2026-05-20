package com.hms.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

  @Autowired private WebTestClient webTestClient;

  @Test
  void contextLoads() {}

  @Test
  void corsPreflight_shouldAllowOrigin() {
    webTestClient
        .options()
        .uri("/auth/login")
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "Content-Type")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("Access-Control-Allow-Origin", "http://localhost:3000")
        .expectHeader()
        .valueEquals("Access-Control-Allow-Credentials", "true");
  }
}
