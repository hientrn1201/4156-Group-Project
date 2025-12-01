package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Test class for ApiLoggingService.
 * Verifies that API calls are properly logged with client information.
 */
class ApiLoggingServiceTest {

  private ApiLoggingService apiLoggingService;
  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {
    apiLoggingService = new ApiLoggingService();

    // Setup log appender to capture log messages
    Logger logger = (Logger) LoggerFactory.getLogger("API_LOGS");
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  void testLogApiRequest() {
    // Given
    String clientId = "test-client-123";
    String method = "POST";
    String endpoint = "/api/v1/documents";
    String requestId = "req-456";
    String userAgent = "TestAgent/1.0";
    String ipAddress = "192.168.1.100";
    String requestBody = "{\"test\": \"data\"}";

    // When
    apiLoggingService.logApiRequest(clientId, method, endpoint, requestId, userAgent, ipAddress,
        requestBody);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("API_REQUEST"));
    assertTrue(logEvent.getFormattedMessage().contains("clientId=test-client-123"));
    assertTrue(logEvent.getFormattedMessage().contains("method=POST"));
    assertTrue(logEvent.getFormattedMessage().contains("endpoint=/api/v1/documents"));
    assertTrue(logEvent.getFormattedMessage().contains("requestId=req-456"));
  }

  @Test
  void testLogApiResponse() {
    // Given
    String clientId = "test-client-456";
    String requestId = "req-789";
    String method = "GET";
    String endpoint = "/api/v1/search/test";
    int statusCode = 200;
    long responseTime = 150L;
    String responseBody = "{\"results\": []}";

    // When
    apiLoggingService.logApiResponse(clientId, requestId, method, endpoint, statusCode,
        responseTime, responseBody);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("API_RESPONSE"));
    assertTrue(logEvent.getFormattedMessage().contains("clientId=test-client-456"));
    assertTrue(logEvent.getFormattedMessage().contains("statusCode=200"));
    assertTrue(logEvent.getFormattedMessage().contains("responseTime=150ms"));
  }

  @Test
  void testLogApiError() {
    // Given
    String clientId = "test-client-789";
    String requestId = "req-101";
    String method = "POST";
    String endpoint = "/api/v1/documents";
    String errorMessage = "File upload failed";
    Exception exception = new RuntimeException("Test exception");

    // When
    apiLoggingService.logApiError(clientId, requestId, method, endpoint, errorMessage, exception);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("API_ERROR"));
    assertTrue(logEvent.getFormattedMessage().contains("clientId=test-client-789"));
    assertTrue(logEvent.getFormattedMessage().contains("error=File upload failed"));
    assertTrue(logEvent.getFormattedMessage().contains("exception=RuntimeException"));
  }

  @Test
  void testGenerateRequestId() {
    // When
    String requestId1 = apiLoggingService.generateRequestId();
    String requestId2 = apiLoggingService.generateRequestId();

    // Then
    assertNotNull(requestId1);
    assertNotNull(requestId2);
    assertTrue(requestId1.length() > 0);
    assertTrue(requestId2.length() > 0);
    // Request IDs should be unique
    assertTrue(!requestId1.equals(requestId2));
  }

  @Test
  void testGetClientId_WithHeader() {
    // Given
    String clientIdHeader = "client-header-123";
    String ipAddress = "192.168.1.200";

    // When
    String clientId = apiLoggingService.getClientId(clientIdHeader, ipAddress);

    // Then
    assertEquals("client-header-123", clientId);
  }

  @Test
  void testGetClientId_WithoutHeader() {
    // Given
    String clientIdHeader = null;
    String ipAddress = "192.168.1.200";

    // When
    String clientId = apiLoggingService.getClientId(clientIdHeader, ipAddress);

    // Then
    assertEquals("client-192-168-1-200", clientId);
  }

  @Test
  void testGetClientId_EmptyHeader() {
    // Given
    String clientIdHeader = "   ";
    String ipAddress = "10.0.0.1";

    // When
    String clientId = apiLoggingService.getClientId(clientIdHeader, ipAddress);

    // Then
    assertEquals("client-10-0-0-1", clientId);
  }

  @Test
  void testGetClientId_NoIpAddress() {
    // Given
    String clientIdHeader = null;
    String ipAddress = null;

    // When
    String clientId = apiLoggingService.getClientId(clientIdHeader, ipAddress);

    // Then
    assertEquals("client-unknown", clientId);
  }

  @Test
  void testLogApiRequest_WithNullRequestBody() {
    // Given
    String clientId = "test-client-null";
    String method = "GET";
    String endpoint = "/api/v1/documents";
    String requestId = "req-null";
    String userAgent = "TestAgent/1.0";
    String ipAddress = "192.168.1.100";
    String requestBody = null;

    // When
    apiLoggingService.logApiRequest(clientId, method, endpoint, requestId, userAgent, ipAddress,
        requestBody);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("body=N/A"));
  }

  @Test
  void testLogApiResponse_WithNullResponseBody() {
    // Given
    String clientId = "test-client-null-resp";
    String requestId = "req-null-resp";
    String method = "DELETE";
    String endpoint = "/api/v1/documents/123";
    int statusCode = 204;
    long responseTime = 50L;
    String responseBody = null;

    // When
    apiLoggingService.logApiResponse(clientId, requestId, method, endpoint, statusCode,
        responseTime, responseBody);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("body=N/A"));
  }

  @Test
  void testLogApiError_WithNullException() {
    // Given
    String clientId = "test-client-null-ex";
    String requestId = "req-null-ex";
    String method = "POST";
    String endpoint = "/api/v1/documents";
    String errorMessage = "Unknown error";
    Exception exception = null;

    // When
    apiLoggingService.logApiError(clientId, requestId, method, endpoint, errorMessage, exception);

    // Then
    assertEquals(1, listAppender.list.size());
    ILoggingEvent logEvent = listAppender.list.get(0);
    assertTrue(logEvent.getFormattedMessage().contains("exception=N/A"));
  }
}