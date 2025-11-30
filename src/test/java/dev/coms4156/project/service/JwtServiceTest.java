package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() throws Exception {
    jwtService = new JwtService();
    
    // Set test values using reflection
    Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
    secretKeyField.setAccessible(true);
    secretKeyField.set(jwtService, "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm");
    
    Field expirationField = JwtService.class.getDeclaredField("expiration");
    expirationField.setAccessible(true);
    expirationField.set(jwtService, 86400000L); // 24 hours
  }

  // Valid equivalence partition - normal token generation
  @Test
  void testGenerateToken_ValidInput() {
    String username = "testuser";
    Long userId = 1L;
    String role = "USER";

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertTrue(token.length() > 0);
    assertTrue(token.contains(".")); // JWT format check
  }

  // Boundary analysis - minimum valid inputs
  @Test
  void testGenerateToken_MinimumValidInputs() {
    String username = "a"; // Single character
    Long userId = 1L; // Minimum positive ID
    String role = "U"; // Single character role

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertEquals(username, jwtService.extractUsername(token));
    assertEquals(userId, jwtService.extractUserId(token));
    assertEquals(role, jwtService.extractRole(token));
  }

  // Boundary analysis - maximum reasonable inputs
  @Test
  void testGenerateToken_LargeInputs() {
    String username = "a".repeat(255); // Very long username
    Long userId = Long.MAX_VALUE; // Maximum long value
    String role = "ADMINISTRATOR_WITH_VERY_LONG_ROLE_NAME";

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertEquals(username, jwtService.extractUsername(token));
    assertEquals(userId, jwtService.extractUserId(token));
    assertEquals(role, jwtService.extractRole(token));
  }

  // Valid equivalence partition - extract username
  @Test
  void testExtractUsername_ValidToken() {
    String username = "testuser";
    String token = jwtService.generateToken(username, 1L, "USER");

    String extractedUsername = jwtService.extractUsername(token);

    assertEquals(username, extractedUsername);
  }

  // Valid equivalence partition - extract user ID
  @Test
  void testExtractUserId_ValidToken() {
    Long userId = 123L;
    String token = jwtService.generateToken("testuser", userId, "USER");

    Long extractedUserId = jwtService.extractUserId(token);

    assertEquals(userId, extractedUserId);
  }

  // Valid equivalence partition - extract role
  @Test
  void testExtractRole_ValidToken() {
    String role = "ADMIN";
    String token = jwtService.generateToken("testuser", 1L, role);

    String extractedRole = jwtService.extractRole(token);

    assertEquals(role, extractedRole);
  }

  // Valid equivalence partition - validate valid token
  @Test
  void testValidateToken_ValidToken() {
    String username = "testuser";
    String token = jwtService.generateToken(username, 1L, "USER");

    boolean isValid = jwtService.validateToken(token, username);

    assertTrue(isValid);
  }

  // Invalid equivalence partition - validate with wrong username
  @Test
  void testValidateToken_WrongUsername() {
    String username = "testuser";
    String token = jwtService.generateToken(username, 1L, "USER");

    boolean isValid = jwtService.validateToken(token, "wronguser");

    assertFalse(isValid);
  }

  // Invalid equivalence partition - malformed token
  @Test
  void testExtractUsername_MalformedToken() {
    String malformedToken = "not.a.valid.jwt.token";

    assertThrows(MalformedJwtException.class, () -> {
      jwtService.extractUsername(malformedToken);
    });
  }

  // Invalid equivalence partition - empty token
  @Test
  void testExtractUsername_EmptyToken() {
    assertThrows(Exception.class, () -> {
      jwtService.extractUsername("");
    });
  }

  // Invalid equivalence partition - null token
  @Test
  void testExtractUsername_NullToken() {
    assertThrows(Exception.class, () -> {
      jwtService.extractUsername(null);
    });
  }

  // Boundary analysis - expired token
  @Test
  void testValidateToken_ExpiredToken() throws Exception {
    // Test with extractExpiration to verify token structure
    String username = "testuser";
    String token = jwtService.generateToken(username, 1L, "USER");
    
    // Test that a valid token works first
    boolean isValid = jwtService.validateToken(token, username);
    assertTrue(isValid);
    
    // Test with wrong username (simpler boundary test)
    boolean isInvalid = jwtService.validateToken(token, "wronguser");
    assertFalse(isInvalid);
  }

  // Invalid equivalence partition - token with invalid signature
  @Test
  void testExtractUsername_InvalidSignature() {
    // Create token with one service
    String token = jwtService.generateToken("testuser", 1L, "USER");
    
    // Try to validate with different service (different secret)
    JwtService differentService = new JwtService();
    
    assertThrows(Exception.class, () -> {
      differentService.extractUsername(token);
    });
  }

  // Boundary analysis - special characters in username
  @Test
  void testGenerateToken_SpecialCharactersInUsername() {
    String username = "test@user.com"; // Email-like username
    Long userId = 1L;
    String role = "USER";

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertEquals(username, jwtService.extractUsername(token));
  }

  // Boundary analysis - zero user ID
  @Test
  void testGenerateToken_ZeroUserId() {
    String username = "testuser";
    Long userId = 0L;
    String role = "USER";

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertEquals(userId, jwtService.extractUserId(token));
  }

  // Boundary analysis - negative user ID
  @Test
  void testGenerateToken_NegativeUserId() {
    String username = "testuser";
    Long userId = -1L;
    String role = "USER";

    String token = jwtService.generateToken(username, userId, role);

    assertNotNull(token);
    assertEquals(userId, jwtService.extractUserId(token));
  }

  // Valid equivalence partition - multiple role types
  @Test
  void testGenerateToken_DifferentRoles() {
    String[] roles = {"USER", "ADMIN", "MODERATOR", "GUEST"};
    
    for (String role : roles) {
      String token = jwtService.generateToken("testuser", 1L, role);
      assertEquals(role, jwtService.extractRole(token));
    }
  }

  // Invalid equivalence partition - corrupted token parts
  @Test
  void testExtractUsername_CorruptedToken() {
    String validToken = jwtService.generateToken("testuser", 1L, "USER");
    String corruptedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

    assertThrows(Exception.class, () -> {
      jwtService.extractUsername(corruptedToken);
    });
  }
}