package dev.coms4156.project.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

  private SecurityConfig securityConfig;
  private JwtAuthenticationFilter jwtAuthenticationFilter;
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @BeforeEach
  void setUp() {
    jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);
    jwtAuthenticationEntryPoint = mock(JwtAuthenticationEntryPoint.class);
    securityConfig = new SecurityConfig(jwtAuthenticationFilter, jwtAuthenticationEntryPoint);
  }

  @Test
  void testPasswordEncoder() {
    PasswordEncoder encoder = securityConfig.passwordEncoder();
    assertNotNull(encoder);
  }

  @Test
  void testAuthenticationManager() throws Exception {
    AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
    AuthenticationManager mockManager = mock(AuthenticationManager.class);
    when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

    AuthenticationManager result = securityConfig.authenticationManager(authConfig);
    assertNotNull(result);
  }

  @Test
  void testCorsConfigurationSource() {
    CorsConfigurationSource source = securityConfig.corsConfigurationSource();
    assertNotNull(source);
  }
}