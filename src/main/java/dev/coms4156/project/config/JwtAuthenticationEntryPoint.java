package dev.coms4156.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Handles authentication failures and returns a proper JSON error response.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ErrorResponse errorResponse = new ErrorResponse(
        "Authentication required. Please provide a valid JWT token in the Authorization header.");

    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}
