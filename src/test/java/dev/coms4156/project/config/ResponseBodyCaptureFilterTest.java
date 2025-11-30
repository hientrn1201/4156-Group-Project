package dev.coms4156.project.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseBodyCaptureFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private ResponseBodyCaptureFilter filter;

  @BeforeEach
  void setUp() {
    filter = new ResponseBodyCaptureFilter();
  }

  @Test
  void testDoFilter_ApiRequest() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/api/v1/documents");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void testDoFilter_NonApiRequest() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/health");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void testInit() throws ServletException {
    filter.init(null);
    // Should not throw exception
  }

  @Test
  void testDestroy() {
    filter.destroy();
    // Should not throw exception
  }
}