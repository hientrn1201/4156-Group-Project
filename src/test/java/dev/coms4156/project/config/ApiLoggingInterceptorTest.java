package dev.coms4156.project.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.service.ApiLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@ExtendWith(MockitoExtension.class)
class ApiLoggingInterceptorTest {

  @Mock
  private ApiLoggingService apiLoggingService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private ApiLoggingInterceptor interceptor;

  @BeforeEach
  void setUp() throws Exception {
    interceptor = new ApiLoggingInterceptor();
    
    // Inject the mocked service using reflection
    Field field = ApiLoggingInterceptor.class.getDeclaredField("apiLoggingService");
    field.setAccessible(true);
    field.set(interceptor, apiLoggingService);
  }

  @Test
  void testPreHandle_WithRequestId() {
    when(request.getHeader("X-Request-ID")).thenReturn("existing-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    boolean result = interceptor.preHandle(request, response, null);

    assertTrue(result);
    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testPreHandle_WithoutRequestId() {
    when(request.getHeader("X-Request-ID")).thenReturn(null);
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.generateRequestId()).thenReturn("generated-id");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    boolean result = interceptor.preHandle(request, response, null);

    assertTrue(result);
    verify(apiLoggingService).generateRequestId();
  }

  @Test
  void testPreHandle_WithForwardedFor() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testPreHandle_WithRealIp() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testPreHandle_PostRequest() {
    ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
    when(wrapper.getHeader("X-Request-ID")).thenReturn("test-id");
    when(wrapper.getHeader("X-Client-ID")).thenReturn("client-123");
    when(wrapper.getMethod()).thenReturn("POST");
    when(wrapper.getRequestURI()).thenReturn("/api/v1/documents");
    when(wrapper.getContentType()).thenReturn("application/json");
    when(wrapper.getContentAsByteArray()).thenReturn("{}".getBytes());
    when(wrapper.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(wrapper, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testPreHandle_MultipartRequest() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getContentType()).thenReturn("multipart/form-data");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testAfterCompletion_Success() {
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(response.getStatus()).thenReturn(200);
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.afterCompletion(request, response, null, null);

    verify(apiLoggingService).logApiResponse(anyString(), anyString(), anyString(), 
        anyString(), any(Integer.class), any(Long.class), any());
  }

  @Test
  void testAfterCompletion_WithException() {
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    Exception testException = new RuntimeException("Test error");
    interceptor.afterCompletion(request, response, null, testException);

    verify(apiLoggingService).logApiError(anyString(), anyString(), anyString(), 
        anyString(), anyString(), any(Exception.class));
  }

  @Test
  void testAfterCompletion_ErrorResponse() {
    ContentCachingResponseWrapper wrapper = mock(ContentCachingResponseWrapper.class);
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(wrapper.getStatus()).thenReturn(404);
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.afterCompletion(request, wrapper, null, null);

    verify(apiLoggingService).logApiResponse(anyString(), anyString(), anyString(), 
        anyString(), any(Integer.class), any(Long.class), any());
  }

  @Test
  void testAfterCompletion_SuccessResponseWithBody() {
    ContentCachingResponseWrapper wrapper = mock(ContentCachingResponseWrapper.class);
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(wrapper.getStatus()).thenReturn(200);
    when(wrapper.getContentAsByteArray()).thenReturn("{\"result\":\"success\"}".getBytes());
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.afterCompletion(request, wrapper, null, null);

    verify(apiLoggingService).logApiResponse(anyString(), anyString(), anyString(), 
        anyString(), any(Integer.class), any(Long.class), any());
  }

  @Test
  void testPreHandle_ExceptionHandling() {
    when(request.getHeader("X-Request-ID")).thenThrow(new RuntimeException("Header error"));

    boolean result = interceptor.preHandle(request, response, null);

    assertTrue(result); // Should still return true despite exception
  }

  @Test
  void testAfterCompletion_ExceptionHandling() {
    when(request.getAttribute("startTime")).thenThrow(new RuntimeException("Attribute error"));

    interceptor.afterCompletion(request, response, null, null);

    // Should not throw exception
  }

  @Test
  void testGetClientIpAddress_EmptyForwardedFor() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getHeader("X-Forwarded-For")).thenReturn("");
    when(request.getHeader("X-Real-IP")).thenReturn(null);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testGetClientIpAddress_EmptyRealIp() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testGetOrGenerateRequestId_EmptyRequestId() {
    when(request.getHeader("X-Request-ID")).thenReturn("   ");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.generateRequestId()).thenReturn("generated-id");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).generateRequestId();
  }

  @Test
  void testExtractRequestBody_PutMethod() {
    ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
    when(wrapper.getHeader("X-Request-ID")).thenReturn("test-id");
    when(wrapper.getHeader("X-Client-ID")).thenReturn("client-123");
    when(wrapper.getMethod()).thenReturn("PUT");
    when(wrapper.getRequestURI()).thenReturn("/api/v1/documents/1");
    when(wrapper.getContentType()).thenReturn("application/json");
    when(wrapper.getContentAsByteArray()).thenReturn("{\"data\":\"test\"}".getBytes());
    when(wrapper.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(wrapper, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testExtractRequestBody_PatchMethod() {
    ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
    when(wrapper.getHeader("X-Request-ID")).thenReturn("test-id");
    when(wrapper.getHeader("X-Client-ID")).thenReturn("client-123");
    when(wrapper.getMethod()).thenReturn("PATCH");
    when(wrapper.getRequestURI()).thenReturn("/api/v1/documents/1");
    when(wrapper.getContentType()).thenReturn("application/json");
    when(wrapper.getContentAsByteArray()).thenReturn("{\"data\":\"test\"}".getBytes());
    when(wrapper.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(wrapper, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testExtractRequestBody_LongBody() {
    ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
    StringBuilder longBody = new StringBuilder();
    for (int i = 0; i < 1100; i++) {
      longBody.append("a");
    }
    
    when(wrapper.getHeader("X-Request-ID")).thenReturn("test-id");
    when(wrapper.getHeader("X-Client-ID")).thenReturn("client-123");
    when(wrapper.getMethod()).thenReturn("POST");
    when(wrapper.getRequestURI()).thenReturn("/api/v1/documents");
    when(wrapper.getContentType()).thenReturn("application/json");
    when(wrapper.getContentAsByteArray()).thenReturn(longBody.toString().getBytes());
    when(wrapper.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(wrapper, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testExtractRequestBody_NotWrapped() {
    when(request.getHeader("X-Request-ID")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getContentType()).thenReturn("application/json");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(request, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testExtractRequestBody_Exception() {
    ContentCachingRequestWrapper wrapper = mock(ContentCachingRequestWrapper.class);
    when(wrapper.getHeader("X-Request-ID")).thenReturn("test-id");
    when(wrapper.getHeader("X-Client-ID")).thenReturn("client-123");
    when(wrapper.getMethod()).thenReturn("POST");
    when(wrapper.getRequestURI()).thenReturn("/api/v1/documents");
    when(wrapper.getContentType()).thenReturn("application/json");
    when(wrapper.getContentAsByteArray()).thenThrow(new RuntimeException("Body error"));
    when(wrapper.getRemoteAddr()).thenReturn("127.0.0.1");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.preHandle(wrapper, response, null);

    verify(apiLoggingService).logApiRequest(anyString(), anyString(), anyString(), 
        anyString(), any(), anyString(), any());
  }

  @Test
  void testExtractResponseBody_LongBody() {
    ContentCachingResponseWrapper wrapper = mock(ContentCachingResponseWrapper.class);
    StringBuilder longBody = new StringBuilder();
    for (int i = 0; i < 1100; i++) {
      longBody.append("b");
    }
    
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(wrapper.getStatus()).thenReturn(200);
    when(wrapper.getContentAsByteArray()).thenReturn(longBody.toString().getBytes());
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.afterCompletion(request, wrapper, null, null);

    verify(apiLoggingService).logApiResponse(anyString(), anyString(), anyString(), 
        anyString(), any(Integer.class), any(Long.class), any());
  }

  @Test
  void testExtractResponseBody_Exception() {
    ContentCachingResponseWrapper wrapper = mock(ContentCachingResponseWrapper.class);
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn("test-id");
    when(request.getHeader("X-Client-ID")).thenReturn("client-123");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/documents");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(wrapper.getStatus()).thenReturn(200);
    when(wrapper.getContentAsByteArray()).thenThrow(new RuntimeException("Response error"));
    when(apiLoggingService.getClientId(anyString(), anyString())).thenReturn("client-123");

    interceptor.afterCompletion(request, wrapper, null, null);

    verify(apiLoggingService).logApiResponse(anyString(), anyString(), anyString(), 
        anyString(), any(Integer.class), any(Long.class), any());
  }

  @Test
  void testAfterCompletion_MissingStartTime() {
    when(request.getAttribute("startTime")).thenReturn(null);
    when(request.getAttribute("requestId")).thenReturn("test-id");

    interceptor.afterCompletion(request, response, null, null);

    // Should not call any logging methods when startTime is null
  }

  @Test
  void testAfterCompletion_MissingRequestId() {
    when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(request.getAttribute("requestId")).thenReturn(null);

    interceptor.afterCompletion(request, response, null, null);

    // Should not call any logging methods when requestId is null
  }
}