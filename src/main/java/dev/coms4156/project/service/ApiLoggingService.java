package dev.coms4156.project.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for logging API calls from clients.
 * Tracks all incoming requests to API endpoints with client identification,
 * request details, response status, and timing information.
 */
@Service
@SuppressWarnings("PMD.MoreThanOneLogger")
public class ApiLoggingService {

  private static final Logger logger = LoggerFactory.getLogger(ApiLoggingService.class);
  private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGS");

  /**
   * Logs an incoming API request.
   *
   * @param clientId    Unique identifier for the client making the request
   * @param method      HTTP method (GET, POST, DELETE, etc.)
   * @param endpoint    API endpoint path
   * @param requestId   Unique request identifier for tracing
   * @param userAgent   Client user agent string
   * @param ipAddress   Client IP address
   * @param requestBody Request body content (for POST/PUT requests)
   */
  public void logApiRequest(String clientId, String method, String endpoint,
                            String requestId, String userAgent, String ipAddress,
                            String requestBody) {
    try {
      String logMessage = String.format(
          "API_REQUEST|clientId=%s|requestId=%s|method=%s|endpoint=%s|"
              + "userAgent=%s|ip=%s|timestamp=%s|body=%s",
          clientId, requestId, method, endpoint, userAgent, ipAddress,
          LocalDateTime.now(), requestBody != null ? requestBody : "N/A");

      apiLogger.info(logMessage);

    } catch (Exception e) {
      logger.error("Failed to log API request for client: {} to endpoint: {}", clientId, endpoint,
          e);
    }
  }

  /**
   * Logs an API response.
   *
   * @param clientId     Unique identifier for the client
   * @param requestId    Unique request identifier for tracing
   * @param method       HTTP method
   * @param endpoint     API endpoint path
   * @param statusCode   HTTP response status code
   * @param responseTime Response time in milliseconds
   * @param responseBody Response body content
   */
  public void logApiResponse(String clientId, String requestId, String method,
                             String endpoint, int statusCode, long responseTime,
                             String responseBody) {
    try {
      String logMessage = String.format(
          "API_RESPONSE|clientId=%s|requestId=%s|method=%s|endpoint=%s|"
              + "statusCode=%d|responseTime=%dms|timestamp=%s|body=%s",
          clientId, requestId, method, endpoint, statusCode, responseTime,
          LocalDateTime.now(), responseBody != null ? responseBody : "N/A");

      apiLogger.info(logMessage);

    } catch (Exception e) {
      logger.error("Failed to log API response for client: {} to endpoint: {}", clientId, endpoint,
          e);
    }
  }

  /**
   * Logs an API error.
   *
   * @param clientId     Unique identifier for the client
   * @param requestId    Unique request identifier for tracing
   * @param method       HTTP method
   * @param endpoint     API endpoint path
   * @param errorMessage Error message
   * @param exception    Exception that occurred
   */
  public void logApiError(String clientId, String requestId, String method,
                          String endpoint, String errorMessage, Exception exception) {
    try {
      String logMessage = String.format(
          "API_ERROR|clientId=%s|requestId=%s|method=%s|endpoint=%s|"
              + "error=%s|timestamp=%s|exception=%s",
          clientId, requestId, method, endpoint, errorMessage,
          LocalDateTime.now(), exception != null ? exception.getClass().getSimpleName() : "N/A");

      apiLogger.error(logMessage);
      logger.error("Logged API error for client: {} to endpoint: {} - {}",
          clientId, endpoint, errorMessage, exception);

    } catch (Exception e) {
      logger.error("Failed to log API error for client: {} to endpoint: {}", clientId, endpoint, e);
    }
  }

  /**
   * Generates a unique request ID for tracing.
   *
   * @return Unique request identifier
   */
  public String generateRequestId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Extracts client ID from request headers or generates a default one.
   *
   * @param clientIdHeader Client ID from request header
   * @param ipAddress      Client IP address as fallback
   * @return Client identifier
   */
  public String getClientId(String clientIdHeader, String ipAddress) {
    if (clientIdHeader != null && !clientIdHeader.trim().isEmpty()) {
      return clientIdHeader.trim();
    }
    // Use IP address as fallback client identifier
    return ipAddress != null
        ? "client-" + ipAddress.replace(".", "-")
        : "client-unknown";
  }
}
