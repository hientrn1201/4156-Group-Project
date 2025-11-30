package dev.coms4156.project.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LoginRequestTest {

  @Test
  void testConstructorAndGetters() {
    LoginRequest request = new LoginRequest("testuser", "password123");
    
    assertEquals("testuser", request.getUsername());
    assertEquals("password123", request.getPassword());
  }

  @Test
  void testDefaultConstructor() {
    LoginRequest request = new LoginRequest();
    assertNotNull(request);
  }

  @Test
  void testSetters() {
    LoginRequest request = new LoginRequest();
    
    request.setUsername("newuser");
    request.setPassword("newpass");
    
    assertEquals("newuser", request.getUsername());
    assertEquals("newpass", request.getPassword());
  }

  @Test
  void testEqualsAndHashCode() {
    LoginRequest request1 = new LoginRequest("user", "pass");
    LoginRequest request2 = new LoginRequest("user", "pass");
    LoginRequest request3 = new LoginRequest("different", "pass");

    assertEquals(request1, request2);
    assertEquals(request1.hashCode(), request2.hashCode());
    assertNotEquals(request1, request3);
  }

  @Test
  void testToString() {
    LoginRequest request = new LoginRequest("testuser", "password");
    assertNotNull(request.toString());
  }
}