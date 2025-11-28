package dev.coms4156.project.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Filter to wrap HTTP responses for body content capture.
 * This enables the logging interceptor to capture response bodies.
 */
@Component
@Order(1)
public class ResponseBodyCaptureFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // No initialization needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Only wrap API requests/responses
    if (httpRequest.getRequestURI().startsWith("/api/")) {
      // Wrap request to allow multiple reads of the body
      ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);

      // Wrap response to capture response body
      ContentCachingResponseWrapper responseWrapper =
          new ContentCachingResponseWrapper(httpResponse);

      try {
        chain.doFilter(requestWrapper, responseWrapper);
      } finally {
        // Copy the cached content to the original response
        responseWrapper.copyBodyToResponse();
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // No cleanup needed
  }
}
