package dev.coms4156.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.coms4156.project.dtos.AuthResponse;
import dev.coms4156.project.dtos.ErrorResponse;
import dev.coms4156.project.dtos.LoginRequest;
import dev.coms4156.project.dtos.RegisterRequest;
import dev.coms4156.project.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Comprehensive API tests for AuthController covering valid/invalid equivalence
 * partitions
 * and boundary analysis for all authentication endpoints.
 */
class ApiAuthControllerTest {

  private AuthenticationService authenticationService;
  private AuthController authController;

  @BeforeEach
  void setUp() {
    authenticationService = mock(AuthenticationService.class);
    authController = new AuthController(authenticationService);
  }

  // ===== REGISTER ENDPOINT TESTS =====

  // Valid equivalence partition - successful registration
  @Test
  void testRegister_ValidRequest_Success() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("testuser")
        .role("USER")
        .message("Registration successful")
        .build();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    AuthResponse responseBody = (AuthResponse) response.getBody();
    assertEquals("jwt-token", responseBody.getToken());
    assertEquals("testuser", responseBody.getUsername());
  }

  // Invalid equivalence partition - user already exists
  @Test
  void testRegister_UserAlreadyExists() {
    RegisterRequest request = new RegisterRequest(
        "existinguser", "existing@example.com", "password123");

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new IllegalArgumentException("User already exists"));

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals("User already exists", errorResponse.getError());
  }

  // Boundary analysis - minimum valid username length (3 characters)
  @Test
  void testRegister_MinimumUsernameLength() {
    RegisterRequest request = new RegisterRequest("abc", "test@example.com", "password123");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("abc")
        .build();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis - maximum valid username length (50 characters)
  @Test
  void testRegister_MaximumUsernameLength() {
    String longUsername = "a".repeat(50); // Exactly 50 characters
    RegisterRequest request = new RegisterRequest(longUsername, "test@example.com", "password123");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username(longUsername)
        .build();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis - minimum valid password length (6 characters)
  @Test
  void testRegister_MinimumPasswordLength() {
    RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "123456");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("testuser")
        .build();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Invalid equivalence partition - invalid email format
  @Test
  void testRegister_InvalidEmailFormat() {
    RegisterRequest request = new RegisterRequest("testuser", "invalid-email", "password123");

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new IllegalArgumentException("Invalid email format"));

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Invalid equivalence partition - empty/null fields
  @Test
  void testRegister_EmptyUsername() {
    RegisterRequest request = new RegisterRequest("", "test@example.com", "password123");

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new IllegalArgumentException("Username is required"));

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testRegister_NullEmail() {
    RegisterRequest request = new RegisterRequest("testuser", null, "password123");

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new IllegalArgumentException("Email is required"));

    ResponseEntity<?> response = authController.register(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Boundary analysis - edge case email formats
  @Test
  void testRegister_ValidEmailEdgeCases() {
    // Test various valid email formats
    String[] validEmails = {
        "a@b.co", // Minimum valid email
        "test.email+tag@example.com", // Email with plus and dot
        "user123@domain-name.org" // Email with numbers and hyphen
    };

    for (String email : validEmails) {
      RegisterRequest request = new RegisterRequest("testuser", email, "password123");
      AuthResponse mockResponse = AuthResponse.builder()
          .token("jwt-token")
          .userId(1L)
          .username("testuser")
          .build();

      when(authenticationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

      ResponseEntity<?> response = authController.register(request);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }

  // ===== LOGIN ENDPOINT TESTS =====

  // Valid equivalence partition - successful login
  @Test
  void testLogin_ValidCredentials_Success() {
    LoginRequest request = new LoginRequest("testuser", "password123");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("testuser")
        .role("USER")
        .message("Login successful")
        .build();

    when(authenticationService.login(any(LoginRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    AuthResponse responseBody = (AuthResponse) response.getBody();
    assertEquals("jwt-token", responseBody.getToken());
    assertEquals("testuser", responseBody.getUsername());
  }

  // Invalid equivalence partition - invalid credentials
  @Test
  void testLogin_InvalidCredentials() {
    LoginRequest request = new LoginRequest("testuser", "wrongpassword");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Invalid credentials"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals("Invalid credentials", errorResponse.getError());
  }

  // Invalid equivalence partition - non-existent user
  @Test
  void testLogin_NonExistentUser() {
    LoginRequest request = new LoginRequest("nonexistent", "password123");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("User not found"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals("User not found", errorResponse.getError());
  }

  // Invalid equivalence partition - empty/null credentials
  @Test
  void testLogin_EmptyUsername() {
    LoginRequest request = new LoginRequest("", "password123");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Username is required"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testLogin_EmptyPassword() {
    LoginRequest request = new LoginRequest("testuser", "");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Password is required"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void testLogin_NullCredentials() {
    LoginRequest request = new LoginRequest(null, null);

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Username and password are required"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // Boundary analysis - whitespace-only credentials
  @Test
  void testLogin_WhitespaceOnlyCredentials() {
    LoginRequest request = new LoginRequest("   ", "   ");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Username and password cannot be blank"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // Boundary analysis - very long credentials
  @Test
  void testLogin_VeryLongCredentials() {
    String longUsername = "a".repeat(1000);
    String longPassword = "b".repeat(1000);
    LoginRequest request = new LoginRequest(longUsername, longPassword);

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Credentials too long"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // Valid equivalence partition - case sensitivity test
  @Test
  void testLogin_CaseSensitiveUsername() {
    LoginRequest request = new LoginRequest("TestUser", "password123");

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new IllegalArgumentException("Invalid credentials"));

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  // Boundary analysis - special characters in credentials
  @Test
  void testLogin_SpecialCharactersInCredentials() {
    LoginRequest request = new LoginRequest("user@domain.com", "p@ssw0rd!");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("user@domain.com")
        .build();

    when(authenticationService.login(any(LoginRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Valid equivalence partition - numeric credentials
  @Test
  void testLogin_NumericCredentials() {
    LoginRequest request = new LoginRequest("user123", "123456");
    AuthResponse mockResponse = AuthResponse.builder()
        .token("jwt-token")
        .userId(1L)
        .username("user123")
        .build();

    when(authenticationService.login(any(LoginRequest.class))).thenReturn(mockResponse);

    ResponseEntity<?> response = authController.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
