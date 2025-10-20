package dev.coms4156.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ControllerTest {

  private final Controller controller = new Controller();

  @Test
  void test_index() {
    ResponseEntity<String> response = controller.index();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Welcome to Knowledge Management Service Powered by AI!", response.getBody());
  }
}