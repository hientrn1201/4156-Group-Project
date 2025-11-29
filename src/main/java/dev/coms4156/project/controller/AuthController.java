package dev.coms4156.project.controller;

import dev.coms4156.project.dtos.AuthResponse;
import dev.coms4156.project.dtos.LoginRequest;
import dev.coms4156.project.dtos.RegisterRequest;
import dev.coms4156.project.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  /**
   * POST /api/v1/auth/register.
   * Register a new user.
   *
   * @param request the registration request
   * @return ResponseEntity containing authentication response with JWT token
   */
  @PostMapping("/register")
  @Operation(summary = "Register a new user",
      description = "Creates a new user account and returns a JWT token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request or user already exists")
  })
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    try {
      AuthResponse response = authenticationService.register(request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new dev.coms4156.project.dtos.ErrorResponse(e.getMessage()));
    }
  }

  /**
   * POST /api/v1/auth/login.
   * Authenticate a user and get a JWT token.
   *
   * @param request the login request
   * @return ResponseEntity containing authentication response with JWT token
   */
  @PostMapping("/login")
  @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login successful",
          content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    try {
      AuthResponse response = authenticationService.login(request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new dev.coms4156.project.dtos.ErrorResponse(e.getMessage()));
    }
  }
}
