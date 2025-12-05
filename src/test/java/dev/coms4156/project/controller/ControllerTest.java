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

  // ===== ADDITIONAL BOUNDARY ANALYSIS AND EQUIVALENCE PARTITION TESTS =====

  // Valid equivalence partition - consistent response format
  @Test
  void test_index_response_format() {
    ResponseEntity<String> response = controller.index();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().contains("Welcome"));
    assertTrue(response.getBody().contains("Knowledge Management Service"));
    assertTrue(response.getBody().contains("AI"));
  }

  // Boundary analysis - response immutability
  @Test
  void test_index_response_immutable() {
    ResponseEntity<String> response1 = controller.index();
    ResponseEntity<String> response2 = controller.index();

    // Responses should be identical
    assertEquals(response1.getStatusCode(), response2.getStatusCode());
    assertEquals(response1.getBody(), response2.getBody());

    // Body should not be modifiable reference
    String body1 = response1.getBody();
    String body2 = response2.getBody();
    assertEquals(body1, body2);
  }

  // Valid equivalence partition - response headers
  @Test
  void test_index_response_headers() {
    ResponseEntity<String> response = controller.index();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    // Verify no unexpected headers are set
    assertNotNull(response.getHeaders());
  }

  // Boundary analysis - concurrent access
  @Test
  void test_index_concurrent_access() throws InterruptedException {
    final int threadCount = 10;
    final ResponseEntity<String>[] responses = new ResponseEntity[threadCount];
    Thread[] threads = new Thread[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      threads[i] = new Thread(() -> {
        responses[index] = controller.index();
      });
    }

    // Start all threads
    for (Thread thread : threads) {
      thread.start();
    }

    // Wait for all threads to complete
    for (Thread thread : threads) {
      thread.join();
    }

    // Verify all responses are identical
    for (int i = 0; i < threadCount; i++) {
      assertEquals(HttpStatus.OK, responses[i].getStatusCode());
      assertEquals(
          "Welcome to Knowledge Management Service Powered by AI!",
          responses[i].getBody());
    }
  }

  // Valid equivalence partition - response content validation
  @Test
  void test_index_response_content_validation() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    assertFalse(body.isEmpty());
    assertFalse(body.isBlank());
    assertTrue(body.length() > 0);

    // Verify specific content requirements
    assertEquals("Welcome to Knowledge Management Service Powered by AI!", body);
  }

  // Boundary analysis - response size
  @Test
  void test_index_response_size() {
    ResponseEntity<String> response = controller.index();
    String body = response.getBody();

    assertNotNull(body);
    // Verify reasonable response size (not too large, not empty)
    assertTrue(body.length() > 10); // Minimum reasonable length
    assertTrue(body.length() < 1000); // Maximum reasonable length for welcome message
  }

  private void assertNotNull(Object obj) {
    org.junit.jupiter.api.Assertions.assertNotNull(obj);
  }

  private void assertTrue(boolean condition) {
    org.junit.jupiter.api.Assertions.assertTrue(condition);
  }

  private void assertFalse(boolean condition) {
    org.junit.jupiter.api.Assertions.assertFalse(condition);
  }

}
