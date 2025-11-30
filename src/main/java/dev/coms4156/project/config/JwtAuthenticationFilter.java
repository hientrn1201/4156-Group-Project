package dev.coms4156.project.config;

import dev.coms4156.project.model.User;
import dev.coms4156.project.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthenticationService authenticationService;

  public JwtAuthenticationFilter(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // Skip JWT authentication for auth endpoints (login, register)
    String requestPath = request.getRequestURI();
    if (requestPath != null && requestPath.startsWith("/api/v1/auth/")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    // Skip if no authorization header or doesn't start with Bearer
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Extract token from header
      final String jwt = authHeader.substring(BEARER_PREFIX.length());

      // Validate token and get user
      User user = authenticationService.validateToken(jwt);

      // Create authentication object
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          user,
          null,
          java.util.Collections.singletonList(
              new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // Set authentication in security context
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception e) {
      // Token validation failed - clear security context
      SecurityContextHolder.clearContext();
      logger.debug("JWT validation failed: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}
