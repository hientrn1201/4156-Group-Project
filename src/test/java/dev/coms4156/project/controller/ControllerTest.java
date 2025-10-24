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

  @Test
  void test_index_multiple_calls() {
    ResponseEntity<String> response1 = controller.index();
    ResponseEntity<String> response2 = controller.index();
    assertEquals(response1.getStatusCode(), response2.getStatusCode());
    assertEquals(response1.getBody(), response2.getBody());
  }

  @Test
  void test_index_response_not_null() {
    ResponseEntity<String> response = controller.index();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Welcome to Knowledge Management Service Powered by AI!", response.getBody());
  }
  
}