package dev.coms4156.project.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

  @Test
  void testBuilder() {
    AuthResponse response = AuthResponse.builder()
        .token("test-token")
        .userId(1L)
        .username("testuser")
        .role("USER")
        .message("Success")
        .build();

    assertEquals("test-token", response.getToken());
    assertEquals("Bearer", response.getType());
    assertEquals(1L, response.getUserId());
    assertEquals("testuser", response.getUsername());
    assertEquals("USER", response.getRole());
    assertEquals("Success", response.getMessage());
  }

  @Test
  void testDefaultConstructor() {
    AuthResponse response = new AuthResponse();
    assertEquals("Bearer", response.getType());
  }

  @Test
  void testAllArgsConstructor() {
    AuthResponse response = new AuthResponse("token", "Custom", 2L, "user", "ADMIN", "msg");
    
    assertEquals("token", response.getToken());
    assertEquals("Custom", response.getType());
    assertEquals(2L, response.getUserId());
    assertEquals("user", response.getUsername());
    assertEquals("ADMIN", response.getRole());
    assertEquals("msg", response.getMessage());
  }

  @Test
  void testSetters() {
    AuthResponse response = new AuthResponse();
    
    response.setToken("new-token");
    response.setType("JWT");
    response.setUserId(3L);
    response.setUsername("newuser");
    response.setRole("MODERATOR");
    response.setMessage("Updated");

    assertEquals("new-token", response.getToken());
    assertEquals("JWT", response.getType());
    assertEquals(3L, response.getUserId());
    assertEquals("newuser", response.getUsername());
    assertEquals("MODERATOR", response.getRole());
    assertEquals("Updated", response.getMessage());
  }

  @Test
  void testToString() {
    AuthResponse response = AuthResponse.builder()
        .token("test-token")
        .build();
    
    assertNotNull(response.toString());
  }
}