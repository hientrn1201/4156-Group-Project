package dev.coms4156.project.service;

import dev.coms4156.project.dtos.AuthResponse;
import dev.coms4156.project.dtos.LoginRequest;
import dev.coms4156.project.dtos.RegisterRequest;
import dev.coms4156.project.model.User;
import dev.coms4156.project.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user authentication and registration.
 */
@Service
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  /**
   * Constructs a new AuthenticationService with required dependencies.
   *
   * @param userRepository  the UserRepository for database operations
   * @param passwordEncoder the PasswordEncoder for password hashing
   * @param jwtService      the JwtService for token generation and validation
   */
  public AuthenticationService(UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return authentication response with JWT token
   * @throws IllegalArgumentException if username or email already exists
   */
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Create new user
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .role(User.Role.USER)
        .isActive(true)
        .build();

    user = userRepository.save(user);

    // Generate JWT token
    String token = jwtService.generateToken(user.getUsername(), user.getId(),
        user.getRole().name());

    return AuthResponse.builder()
        .token(token)
        .userId(user.getId())
        .username(user.getUsername())
        .role(user.getRole().name())
        .message("User registered successfully")
        .build();
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param request the login request
   * @return authentication response with JWT token
   * @throws IllegalArgumentException if credentials are invalid
   */
  public AuthResponse login(LoginRequest request) {
    Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("Invalid username or password");
    }

    User user = userOpt.get();

    // Check if user is active
    if (!user.getIsActive()) {
      throw new IllegalArgumentException("User account is deactivated");
    }

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid username or password");
    }

    // Generate JWT token
    String token = jwtService.generateToken(user.getUsername(), user.getId(),
        user.getRole().name());

    return AuthResponse.builder()
        .token(token)
        .userId(user.getId())
        .username(user.getUsername())
        .role(user.getRole().name())
        .message("Login successful")
        .build();
  }

  /**
   * Validates a JWT token and returns user information.
   *
   * @param token the JWT token
   * @return the user if token is valid
   * @throws IllegalArgumentException if token is invalid
   */
  public User validateToken(String token) {
    try {
      String username = jwtService.extractUsername(token);
      Optional<User> userOpt = userRepository.findByUsername(username);

      if (userOpt.isEmpty()) {
        throw new IllegalArgumentException("Invalid token");
      }

      User user = userOpt.get();

      if (!user.getIsActive()) {
        throw new IllegalArgumentException("User account is deactivated");
      }

      if (!jwtService.validateToken(token, username)) {
        throw new IllegalArgumentException("Invalid or expired token");
      }

      return user;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid token: " + e.getMessage());
    }
  }
}
