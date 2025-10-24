package dev.coms4156.project.config;

import dev.coms4156.project.service.ApiLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Interceptor to automatically log all API requests and responses.
 * Captures request details, client information, and response data for
 * comprehensive logging.
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(ApiLoggingInterceptor.class);

  @Autowired
  private ApiLoggingService apiLoggingService;

  private static final String REQUEST_ID_HEADER = "X-Request-ID";
  private static final String CLIENT_ID_HEADER = "X-Client-ID";
  private static final String START_TIME_ATTRIBUTE = "startTime";
  private static final String REQUEST_ID_ATTRIBUTE = "requestId";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) {
    try {
      long startTime = System.currentTimeMillis();
      String requestId = getOrGenerateRequestId(request);

      // Store start time and request ID for use in postHandle
      request.setAttribute(START_TIME_ATTRIBUTE, startTime);
      request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

      // Extract client information
      String clientId = apiLoggingService.getClientId(
          request.getHeader(CLIENT_ID_HEADER),
          getClientIpAddress(request));

      // Extract request details
      String method = request.getMethod();
      String endpoint = request.getRequestURI();
      String userAgent = request.getHeader("User-Agent");
      String requestBody = extractRequestBody(request);

      // Log the incoming request
      apiLoggingService.logApiRequest(
          clientId, method, endpoint, requestId, userAgent,
          getClientIpAddress(request), requestBody);

    } catch (Exception e) {
      logger.error("Error in preHandle of ApiLoggingInterceptor", e);
    }

    return true; // Continue with request processing
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) {
    try {
      Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
      String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);

      if (startTime != null && requestId != null) {
        long responseTime = System.currentTimeMillis() - startTime;

        // Extract client information
        String clientId = apiLoggingService.getClientId(
            request.getHeader(CLIENT_ID_HEADER),
            getClientIpAddress(request));

        // Extract response details
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        int statusCode = response.getStatus();
        String responseBody = extractResponseBody(response);

        if (ex != null) {
          // Log the error
          apiLoggingService.logApiError(
              clientId, requestId, method, endpoint,
              ex.getMessage(), ex);
        } else {
          // Log the response
          apiLoggingService.logApiResponse(
              clientId, requestId, method, endpoint, statusCode,
              responseTime, responseBody);
        }
      }

    } catch (Exception e) {
      logger.error("Error in afterCompletion of ApiLoggingInterceptor", e);
    }
  }

  /**
   * Extracts the client IP address from the request.
   * Handles proxy headers like X-Forwarded-For and X-Real-IP.
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0].trim();
    }

    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isEmpty()) {
      return realIp;
    }

    return request.getRemoteAddr();
  }

  /**
   * Gets the request ID from headers or generates a new one.
   */
  private String getOrGenerateRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.trim().isEmpty()) {
      requestId = apiLoggingService.generateRequestId();
    }
    return requestId;
  }

  /**
   * Extracts the request body for logging purposes.
   * Only extracts body for POST/PUT/PATCH requests.
   */
  private String extractRequestBody(HttpServletRequest request) {
    String method = request.getMethod();
    if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
      return null;
    }

    try {
      // For multipart requests, don't try to read the body as it's already consumed
      String contentType = request.getContentType();
      if (contentType != null && contentType.contains("multipart/form-data")) {
        return "[MULTIPART_DATA]";
      }

      // Read the request body
      byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
      if (body.length > 0) {
        String bodyString = new String(body, StandardCharsets.UTF_8);
        // Truncate very long request bodies
        return bodyString.length() > 1000 ? bodyString.substring(0, 1000) + "...[TRUNCATED]" :
            bodyString;
      }

    } catch (IOException e) {
      logger.warn("Could not read request body for logging", e);
      return "[ERROR_READING_BODY]";
    }

    return null;
  }

  /**
   * Extracts the response body for logging purposes.
   * Only extracts body for successful responses (2xx status codes).
   */
  private String extractResponseBody(HttpServletResponse response) {
    int statusCode = response.getStatus();
    if (statusCode < 200 || statusCode >= 300) {
      return null; // Don't log response body for error responses
    }

    try {
      // Try to get response body from ContentCachingResponseWrapper
      if (response instanceof ContentCachingResponseWrapper) {
        ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) response;
        byte[] body = wrapper.getContentAsByteArray();
        if (body.length > 0) {
          String bodyString = new String(body, StandardCharsets.UTF_8);
          // Truncate very long response bodies
          return bodyString.length() > 1000 ? bodyString.substring(0, 1000) + "...[TRUNCATED]" :
              bodyString;
        }
      }

    } catch (Exception e) {
      logger.warn("Could not read response body for logging", e);
      return "[ERROR_READING_RESPONSE]";
    }

    return null;
  }

}
