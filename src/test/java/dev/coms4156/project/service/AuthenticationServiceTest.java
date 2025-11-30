package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.coms4156.project.dtos.AuthResponse;
import dev.coms4156.project.dtos.LoginRequest;
import dev.coms4156.project.dtos.RegisterRequest;
import dev.coms4156.project.model.User;
import dev.coms4156.project.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService);
  }

  // Valid equivalence partition tests
  @Test
  void testRegister_ValidInput() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("testuser");
    request.setEmail("test@example.com");
    request.setPassword("password123");

    when(userRepository.existsByUsername("testuser")).thenReturn(false);
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
    when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("jwt-token");

    User savedUser = User.builder()
        .id(1L)
        .username("testuser")
        .email("test@example.com")
        .role(User.Role.USER)
        .isActive(true)
        .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    AuthResponse response = authenticationService.register(request);

    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    assertEquals("testuser", response.getUsername());
  }

  // Invalid equivalence partition - duplicate username
  @Test
  void testRegister_DuplicateUsername() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("existinguser");
    request.setEmail("test@example.com");
    request.setPassword("password123");

    when(userRepository.existsByUsername("existinguser")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.register(request);
    });
  }

  // Invalid equivalence partition - duplicate email
  @Test
  void testRegister_DuplicateEmail() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("testuser");
    request.setEmail("existing@example.com");
    request.setPassword("password123");

    when(userRepository.existsByUsername("testuser")).thenReturn(false);
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.register(request);
    });
  }

  // Boundary analysis - minimum valid username length
  @Test
  void testRegister_MinimumUsernameLength() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("a"); // Single character
    request.setEmail("test@example.com");
    request.setPassword("password123");

    when(userRepository.existsByUsername("a")).thenReturn(false);
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
    when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("jwt-token");

    User savedUser = User.builder()
        .id(1L)
        .username("a")
        .email("test@example.com")
        .role(User.Role.USER)
        .isActive(true)
        .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    AuthResponse response = authenticationService.register(request);
    assertNotNull(response);
  }

  // Valid login test
  @Test
  void testLogin_ValidCredentials() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("password123");

    User user = User.builder()
        .id(1L)
        .username("testuser")
        .passwordHash("hashedPassword")
        .role(User.Role.USER)
        .isActive(true)
        .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
    when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn("jwt-token");

    AuthResponse response = authenticationService.login(request);

    assertNotNull(response);
    assertEquals("jwt-token", response.getToken());
    assertEquals("testuser", response.getUsername());
  }

  // Invalid equivalence partition - nonexistent user
  @Test
  void testLogin_NonexistentUser() {
    LoginRequest request = new LoginRequest();
    request.setUsername("nonexistent");
    request.setPassword("password123");

    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.login(request);
    });
  }

  // Invalid equivalence partition - wrong password
  @Test
  void testLogin_WrongPassword() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("wrongpassword");

    User user = User.builder()
        .id(1L)
        .username("testuser")
        .passwordHash("hashedPassword")
        .role(User.Role.USER)
        .isActive(true)
        .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.login(request);
    });
  }

  // Invalid equivalence partition - inactive user
  @Test
  void testLogin_InactiveUser() {
    LoginRequest request = new LoginRequest();
    request.setUsername("testuser");
    request.setPassword("password123");

    User user = User.builder()
        .id(1L)
        .username("testuser")
        .passwordHash("hashedPassword")
        .role(User.Role.USER)
        .isActive(false)
        .build();

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.login(request);
    });
  }

  // Valid token validation
  @Test
  void testValidateToken_ValidToken() {
    String token = "valid-jwt-token";
    User user = User.builder()
        .id(1L)
        .username("testuser")
        .role(User.Role.USER)
        .isActive(true)
        .build();

    when(jwtService.extractUsername(token)).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, "testuser")).thenReturn(true);

    User result = authenticationService.validateToken(token);

    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
  }

  // Invalid equivalence partition - invalid token
  @Test
  void testValidateToken_InvalidToken() {
    String token = "invalid-token";

    when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.validateToken(token);
    });
  }

  // Invalid equivalence partition - token for nonexistent user
  @Test
  void testValidateToken_NonexistentUser() {
    String token = "valid-jwt-token";

    when(jwtService.extractUsername(token)).thenReturn("nonexistent");
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.validateToken(token);
    });
  }

  // Invalid equivalence partition - token for inactive user
  @Test
  void testValidateToken_InactiveUser() {
    String token = "valid-jwt-token";
    User user = User.builder()
        .id(1L)
        .username("testuser")
        .role(User.Role.USER)
        .isActive(false)
        .build();

    when(jwtService.extractUsername(token)).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.validateToken(token);
    });
  }

  // Invalid equivalence partition - expired token
  @Test
  void testValidateToken_ExpiredToken() {
    String token = "expired-jwt-token";
    User user = User.builder()
        .id(1L)
        .username("testuser")
        .role(User.Role.USER)
        .isActive(true)
        .build();

    when(jwtService.extractUsername(token)).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, "testuser")).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> {
      authenticationService.validateToken(token);
    });
  }
}