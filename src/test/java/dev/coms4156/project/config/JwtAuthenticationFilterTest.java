package dev.coms4156.project.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.User;
import dev.coms4156.project.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationService);
    SecurityContextHolder.clearContext();
  }

  @Test
  void testDoFilterInternal_AuthEndpoint() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authenticationService, never()).validateToken(anyString());
  }

  @Test
  void testDoFilterInternal_NoAuthHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authenticationService, never()).validateToken(anyString());
  }

  @Test
  void testDoFilterInternal_InvalidBearerPrefix() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getHeader("Authorization")).thenReturn("Basic token");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authenticationService, never()).validateToken(anyString());
  }

  @Test
  void testDoFilterInternal_ValidToken() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
    
    User user = User.builder()
        .id(1L)
        .username("testuser")
        .role(User.Role.USER)
        .build();
    when(authenticationService.validateToken("valid-token")).thenReturn(user);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authenticationService).validateToken("valid-token");
  }

  @Test
  void testDoFilterInternal_InvalidToken() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(authenticationService.validateToken("invalid-token"))
        .thenThrow(new RuntimeException("Invalid token"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(authenticationService).validateToken("invalid-token");
  }
}