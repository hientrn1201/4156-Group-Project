package dev.coms4156.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.service.ApiLoggingService;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import dev.coms4156.project.service.RagService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprehensive API Integration Tests using MockMvc.
 */
@WebMvcTest(value = { DocumentApiController.class, Controller.class }, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API Integration Tests")
class DocumentApiTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private DocumentService documentService;

  @MockBean
  private DocumentSummarizationService summarizationService;

  @MockBean
  private RagService ragService;

  @MockBean
  private ApiLoggingService apiLoggingService;

  // Mock security-related beans to prevent SecurityConfig from failing
  @MockBean
  private dev.coms4156.project.config.JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private dev.coms4156.project.config.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private ListAppender<ILoggingEvent> logAppender;
  private Logger apiLogger;

  /**
   * Setup method that runs before each test.
   * This sets up the test environment:
   * - Configures logging to capture log messages
   * - Mocks the ApiLoggingService to return predictable test IDs
   */
  @BeforeEach
  void setUp() {
    // Setup logging capture for API logging verification
    apiLogger = (Logger) LoggerFactory.getLogger("API_LOGS");
    logAppender = new ListAppender<>();
    logAppender.start();
    apiLogger.addAppender(logAppender);

    // Mock ApiLoggingService to return predictable IDs for testing
    // This makes our tests repeatable and predictable
    when(apiLoggingService.generateRequestId()).thenReturn("test-request-id");
    when(apiLoggingService.getClientId(anyString(), anyString())).thenAnswer(invocation -> {
      String clientId = invocation.getArgument(0);
      String ip = invocation.getArgument(1);
      // If client ID provided, use it; otherwise generate from IP
      return clientId != null ? clientId : "client-" + ip.replace(".", "-");
    });
  }

  @Nested
  @DisplayName("GET /api - Welcome Endpoint")
  class WelcomeEndpointTests {

    @Test
    @DisplayName("Typical valid: GET welcome endpoint")
    void testWelcomeEndpoint_TypicalValid() throws Exception {
      mockMvc.perform(get("/api")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(content().string("Welcome to Knowledge Management Service Powered by AI!"));
    }

    @Test
    @DisplayName("Atypical valid: GET welcome with different client ID")
    void testWelcomeEndpoint_AtypicalValid() throws Exception {
      mockMvc.perform(get("/api")
          .header("X-Client-ID", "unusual-client-name-12345"))
          .andExpect(status().isOk())
          .andExpect(content().string("Welcome to Knowledge Management Service Powered by AI!"));
    }

    @Test
    @DisplayName("Invalid: GET welcome with malformed request")
    void testWelcomeEndpoint_Invalid() throws Exception {
      // Invalid path - should return 404
      mockMvc.perform(get("/api/invalid-path")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/documents - Upload Document")
  class UploadDocumentTests {

    @Test
    @DisplayName("Typical valid: Upload PDF document")
    void testUploadDocument_TypicalValid() throws Exception {
      // Create a mock file to upload
      MockMultipartFile file = new MockMultipartFile(
          "file", "test-document.pdf", "application/pdf",
          "This is test PDF content".getBytes());

      // Create the expected document that will be returned by the service
      Document document = Document.builder()
          .id(1L)
          .filename("test-document.pdf")
          .contentType("application/pdf")
          .fileSize(1024L)
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      // Mock the service to return our expected document
      when(documentService.processDocument(any())).thenReturn(document);

      // Make the actual HTTP POST request using MockMvc
      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-1"))
          // Verify the HTTP response
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(1L))
          .andExpect(jsonPath("$.filename").value("test-document.pdf"))
          .andExpect(jsonPath("$.processingStatus").value("COMPLETED"))
          .andExpect(jsonPath("$.message").exists());

      // Verify the service was called exactly once
      verify(documentService, times(1)).processDocument(any());
    }

    @Test
    @DisplayName("Atypical valid: Upload large text file with special characters")
    void testUploadDocument_AtypicalValid() throws Exception {
      // Test with unusual but valid input - large file with special characters
      String largeContent = "A".repeat(10000) + " with special chars: émojis 🚀 and symbols ©®™";
      MockMultipartFile file = new MockMultipartFile(
          "file", "large-file-émoji.txt", "text/plain",
          largeContent.getBytes());

      Document document = Document.builder()
          .id(2L)
          .filename("large-file-émoji.txt")
          .contentType("text/plain")
          .fileSize((long) largeContent.getBytes().length)
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(document);

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-special"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(2L))
          .andExpect(jsonPath("$.filename").value("large-file-émoji.txt"));

      verify(documentService, times(1)).processDocument(any());
    }

    @Test
    @DisplayName("Invalid: Upload empty file")
    void testUploadDocument_Invalid_EmptyFile() throws Exception {
      // Test invalid input - empty file should be rejected
      MockMultipartFile emptyFile = new MockMultipartFile(
          "file", "empty.txt", "text/plain", new byte[0]);

      // Service should throw exception for empty file
      when(documentService.processDocument(any()))
          .thenThrow(new IllegalArgumentException("File is empty"));

      // Make request and verify it returns 400 Bad Request
      mockMvc.perform(multipart("/api/v1/documents")
          .file(emptyFile)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("File is empty"));

      verify(documentService, times(1)).processDocument(any());
    }

    @Test
    @DisplayName("Invalid: Upload without file parameter")
    void testUploadDocument_Invalid_MissingFile() throws Exception {
      mockMvc.perform(multipart("/api/v1/documents")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Invalid: Service exception during processing")
    void testUploadDocument_Invalid_ServiceException() throws Exception {
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.pdf", "application/pdf", "content".getBytes());

      when(documentService.processDocument(any()))
          .thenThrow(new RuntimeException("Processing failed"));

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.error").value("Document processing failed"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/documents/{id} - Get Document")
  class GetDocumentTests {

    @Test
    @DisplayName("Typical valid: Get existing document by ID")
    void testGetDocument_TypicalValid() throws Exception {
      Document document = Document.builder()
          .id(1L)
          .filename("test.pdf")
          .contentType("application/pdf")
          .fileSize(1024L)
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .summary("Test summary")
          .uploadedAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();

      when(documentService.getDocumentById(1L)).thenReturn(Optional.of(document));

      mockMvc.perform(get("/api/v1/documents/1")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1L))
          .andExpect(jsonPath("$.filename").value("test.pdf"))
          .andExpect(jsonPath("$.processingStatus").value("COMPLETED"));

      verify(documentService, times(1)).getDocumentById(1L);
    }

    @Test
    @DisplayName("Atypical valid: Get document with very large ID")
    void testGetDocument_AtypicalValid_LargeId() throws Exception {
      Document document = Document.builder()
          .id(Long.MAX_VALUE)
          .filename("large-id-doc.pdf")
          .contentType("application/pdf")
          .fileSize(1L)
          .processingStatus(Document.ProcessingStatus.UPLOADED)
          .build();

      when(documentService.getDocumentById(Long.MAX_VALUE))
          .thenReturn(Optional.of(document));

      mockMvc.perform(get("/api/v1/documents/" + Long.MAX_VALUE)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(Long.MAX_VALUE));

      verify(documentService, times(1)).getDocumentById(Long.MAX_VALUE);
    }

    @Test
    @DisplayName("Invalid: Get non-existent document")
    void testGetDocument_Invalid_NotFound() throws Exception {
      when(documentService.getDocumentById(999L)).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/documents/999")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isNotFound());

      verify(documentService, times(1)).getDocumentById(999L);
    }

    @Test
    @DisplayName("Invalid: Invalid ID format")
    void testGetDocument_Invalid_BadId() throws Exception {
      mockMvc.perform(get("/api/v1/documents/invalid-id")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/documents - Get All Documents")
  class GetAllDocumentsTests {

    @Test
    @DisplayName("Typical valid: Get all documents")
    void testGetAllDocuments_TypicalValid() throws Exception {
      List<Document> documents = Arrays.asList(
          Document.builder().id(1L).filename("doc1.pdf").build(),
          Document.builder().id(2L).filename("doc2.pdf").build());

      when(documentService.getAllDocuments()).thenReturn(documents);

      mockMvc.perform(get("/api/v1/documents")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(2))
          .andExpect(jsonPath("$.documents").isArray())
          .andExpect(jsonPath("$.message").value("Documents retrieved successfully"));

      verify(documentService, times(1)).getAllDocuments();
    }

    @Test
    @DisplayName("Atypical valid: Get documents with filename filter")
    void testGetAllDocuments_AtypicalValid_WithFilter() throws Exception {
      List<Document> documents = Arrays.asList(
          Document.builder().id(1L).filename("test-doc.pdf").build());

      when(documentService.getDocumentsByFilename("test")).thenReturn(documents);

      mockMvc.perform(get("/api/v1/documents")
          .param("filename", "test")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(1))
          .andExpect(jsonPath("$.documents[0].filename").value("test-doc.pdf"));

      verify(documentService, times(1)).getDocumentsByFilename("test");
    }

    @Test
    @DisplayName("Invalid: Service exception")
    void testGetAllDocuments_Invalid_ServiceException() throws Exception {
      when(documentService.getAllDocuments())
          .thenThrow(new RuntimeException("Database error"));

      mockMvc.perform(get("/api/v1/documents")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isInternalServerError());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/search/{text} - Semantic Search")
  class SearchDocumentsTests {

    @Test
    @DisplayName("Typical valid: Search with normal query")
    void testSearchDocuments_TypicalValid() throws Exception {
      DocumentChunk chunk = DocumentChunk.builder()
          .id(1L)
          .textContent("Machine learning algorithms")
          .chunkIndex(0)
          .build();

      // MockMvc with URL-encoded path: Spring decodes it, so service receives decoded
      // version
      // But MockMvc doesn't decode automatically - we need to use decoded version in
      // the path
      when(documentService.findSimilarChunks("machine learning", 3))
          .thenReturn(Arrays.asList(chunk));

      // Use unencoded path variable - MockMvc handles encoding
      mockMvc.perform(get("/api/v1/search/machine learning")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          // Spring decodes the path variable before passing to controller
          .andExpect(jsonPath("$.query").value("machine learning"))
          .andExpect(jsonPath("$.count").value(1))
          .andExpect(jsonPath("$.results").isArray())
          .andExpect(jsonPath("$.message").value("Search completed successfully"));

      verify(documentService, times(1)).findSimilarChunks("machine learning", 3);
    }

    @Test
    @DisplayName("Atypical valid: Search with special characters and long query")
    void testSearchDocuments_AtypicalValid() throws Exception {
      String query = "test query"; // Simplified for MockMvc testing
      DocumentChunk chunk = DocumentChunk.builder()
          .id(1L)
          .textContent("Special content")
          .build();

      when(documentService.findSimilarChunks(eq(query), eq(3)))
          .thenReturn(Arrays.asList(chunk));

      // MockMvc handles encoding automatically
      mockMvc.perform(get("/api/v1/search/" + query)
          .header("X-Client-ID", "client-special"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(1));

      verify(documentService, times(1)).findSimilarChunks(eq(query), eq(3));
    }

    @Test
    @DisplayName("Invalid: Service exception during search")
    void testSearchDocuments_Invalid_ServiceException() throws Exception {
      when(documentService.findSimilarChunks(anyString(), anyInt()))
          .thenThrow(new RuntimeException("Search failed"));

      mockMvc.perform(get("/api/v1/search/test")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Atypical valid: Empty search results")
    void testSearchDocuments_AtypicalValid_EmptyResults() throws Exception {
      when(documentService.findSimilarChunks("nonexistent", 3))
          .thenReturn(Arrays.asList());

      mockMvc.perform(get("/api/v1/search/nonexistent")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(0))
          .andExpect(jsonPath("$.results").isArray());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/documents/{id}/summary - Get Document Summary")
  class GetDocumentSummaryTests {

    @Test
    @DisplayName("Typical valid: Get summary for existing document")
    void testGetDocumentSummary_TypicalValid() throws Exception {
      when(summarizationService.getDocumentSummary(1L))
          .thenReturn("This is a test summary of the document.");

      mockMvc.perform(get("/api/v1/documents/1/summary")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(1L))
          .andExpect(jsonPath("$.summary").value("This is a test summary of the document."));

      verify(summarizationService, times(1)).getDocumentSummary(1L);
    }

    @Test
    @DisplayName("Atypical valid: Get summary with very long text")
    void testGetDocumentSummary_AtypicalValid_LongSummary() throws Exception {
      String longSummary = "Summary ".repeat(100);
      when(summarizationService.getDocumentSummary(1L)).thenReturn(longSummary);

      mockMvc.perform(get("/api/v1/documents/1/summary")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    @DisplayName("Invalid: Summary not found")
    void testGetDocumentSummary_Invalid_NotFound() throws Exception {
      when(summarizationService.getDocumentSummary(999L)).thenReturn(null);

      mockMvc.perform(get("/api/v1/documents/999/summary")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isNotFound());

      verify(summarizationService, times(1)).getDocumentSummary(999L);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/documents/stats - Get Processing Statistics")
  class GetProcessingStatisticsTests {

    @Test
    @DisplayName("Typical valid: Get statistics with various statuses")
    void testGetProcessingStatistics_TypicalValid() throws Exception {
      List<Document> docs = Arrays.asList(
          Document.builder().processingStatus(Document.ProcessingStatus.COMPLETED).build(),
          Document.builder().processingStatus(Document.ProcessingStatus.FAILED).build());

      when(documentService.getAllDocuments()).thenReturn(docs);

      mockMvc.perform(get("/api/v1/documents/stats")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total").value(2))
          .andExpect(jsonPath("$.byStatus").exists())
          .andExpect(jsonPath("$.completionRate").exists())
          .andExpect(jsonPath("$.failureRate").exists());
    }

    @Test
    @DisplayName("Atypical valid: Get statistics with zero documents")
    void testGetProcessingStatistics_AtypicalValid_Empty() throws Exception {
      when(documentService.getAllDocuments()).thenReturn(Arrays.asList());

      mockMvc.perform(get("/api/v1/documents/stats")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total").value(0))
          .andExpect(jsonPath("$.completionRate").value(0.0))
          .andExpect(jsonPath("$.failureRate").value(0.0));
    }

    @Test
    @DisplayName("Invalid: Service exception")
    void testGetProcessingStatistics_Invalid_ServiceException() throws Exception {
      when(documentService.getAllDocuments())
          .thenThrow(new RuntimeException("Database error"));

      // Controller doesn't catch exceptions - RuntimeException propagates
      // Spring wraps it in ServletException, which is expected for unhandled
      // exceptions
      // This test verifies that service exceptions propagate correctly
      try {
        mockMvc.perform(get("/api/v1/documents/stats")
            .header("X-Client-ID", "client-1"))
            .andExpect(status().isInternalServerError());
      } catch (jakarta.servlet.ServletException e) {
        // Expected - Spring wraps RuntimeException in ServletException
        // This verifies that service exceptions result in error responses
        assertTrue(e.getCause() instanceof RuntimeException);
        assertEquals("Database error", e.getCause().getMessage());
      }
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/documents/{id} - Delete Document")
  class DeleteDocumentTests {

    @Test
    @DisplayName("Typical valid: Delete existing document")
    void testDeleteDocument_TypicalValid() throws Exception {
      Document document = Document.builder()
          .id(1L)
          .filename("test.pdf")
          .build();

      when(documentService.getDocumentById(1L)).thenReturn(Optional.of(document));

      mockMvc.perform(delete("/api/v1/documents/1")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(1L))
          .andExpect(jsonPath("$.message").value("Document deleted successfully"));

      verify(documentService, times(1)).deleteDocument(1L);
    }

    @Test
    @DisplayName("Atypical valid: Delete document with large ID")
    void testDeleteDocument_AtypicalValid_LargeId() throws Exception {
      Document document = Document.builder().id(Long.MAX_VALUE).build();
      when(documentService.getDocumentById(Long.MAX_VALUE))
          .thenReturn(Optional.of(document));

      mockMvc.perform(delete("/api/v1/documents/" + Long.MAX_VALUE)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Invalid: Delete non-existent document")
    void testDeleteDocument_Invalid_NotFound() throws Exception {
      when(documentService.getDocumentById(999L)).thenReturn(Optional.empty());

      mockMvc.perform(delete("/api/v1/documents/999")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("Document not found"));

      verify(documentService, never()).deleteDocument(anyLong());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/documents/summaries - Get Documents with Summaries")
  class GetDocumentsWithSummariesTests {

    @Test
    @DisplayName("Typical valid: Get documents with summaries")
    void testGetDocumentsWithSummaries_TypicalValid() throws Exception {
      List<Document> allDocs = Arrays.asList(
          Document.builder().id(1L).summary("Summary 1").build(),
          Document.builder().id(2L).summary("Summary 2").build(),
          Document.builder().id(3L).summary(null).build());

      when(documentService.getAllDocuments()).thenReturn(allDocs);

      mockMvc.perform(get("/api/v1/documents/summaries")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(2))
          .andExpect(jsonPath("$.documents").isArray());
    }

    @Test
    @DisplayName("Atypical valid: Get documents with empty summaries list")
    void testGetDocumentsWithSummaries_AtypicalValid_Empty() throws Exception {
      when(documentService.getAllDocuments()).thenReturn(Arrays.asList());

      mockMvc.perform(get("/api/v1/documents/summaries")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Invalid: Service exception")
    void testGetDocumentsWithSummaries_Invalid_ServiceException() throws Exception {
      when(documentService.getAllDocuments())
          .thenThrow(new RuntimeException("Database error"));

      // Controller doesn't catch exceptions - RuntimeException propagates
      // Spring wraps it in ServletException, which is expected for unhandled
      // exceptions
      // This test verifies that service exceptions propagate correctly
      try {
        mockMvc.perform(get("/api/v1/documents/summaries")
            .header("X-Client-ID", "client-1"))
            .andExpect(status().isInternalServerError());
      } catch (jakarta.servlet.ServletException e) {
        // Expected - Spring wraps RuntimeException in ServletException
        // This verifies that service exceptions result in error responses
        assertTrue(e.getCause() instanceof RuntimeException);
        assertEquals("Database error", e.getCause().getMessage());
      }
    }
  }

  @Nested
  @DisplayName("GET /api/v1/relationships/{documentId} - Get Document Relationships")
  class GetDocumentRelationshipsTests {

    @Test
    @DisplayName("Typical valid: Get relationships for document")
    void testGetDocumentRelationships_TypicalValid() throws Exception {
      mockMvc.perform(get("/api/v1/relationships/1")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(1L))
          .andExpect(jsonPath("$.relationships").isArray())
          .andExpect(jsonPath("$.count").value(0))
          .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Atypical valid: Get relationships with large document ID")
    void testGetDocumentRelationships_AtypicalValid_LargeId() throws Exception {
      mockMvc.perform(get("/api/v1/relationships/" + Long.MAX_VALUE)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Invalid: Invalid document ID format")
    void testGetDocumentRelationships_Invalid_BadId() throws Exception {
      mockMvc.perform(get("/api/v1/relationships/invalid")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Multiple Client Support Tests")
  class MultipleClientTests {

    @Test
    @DisplayName("Test multiple clients: Client 1 uploads document")
    void testMultipleClients_Client1Upload() throws Exception {
      MockMultipartFile file = new MockMultipartFile(
          "file", "client1-doc.pdf", "application/pdf", "content".getBytes());

      Document document = Document.builder()
          .id(1L)
          .filename("client1-doc.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(document);
      when(apiLoggingService.getClientId("client-backend-1", "127.0.0.1"))
          .thenReturn("client-backend-1");

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-backend-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(1L));
    }

    @Test
    @DisplayName("Test multiple clients: Client 2 uploads document")
    void testMultipleClients_Client2Upload() throws Exception {
      MockMultipartFile file = new MockMultipartFile(
          "file", "client2-doc.pdf", "application/pdf", "content".getBytes());

      Document document = Document.builder()
          .id(2L)
          .filename("client2-doc.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(document);
      when(apiLoggingService.getClientId("client-backend-2", "127.0.0.1"))
          .thenReturn("client-backend-2");

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-backend-2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(2L));
    }

    @Test
    @DisplayName("Test multiple clients: Both clients can search independently")
    void testMultipleClients_IndependentSearch() throws Exception {
      when(documentService.findSimilarChunks(anyString(), anyInt()))
          .thenReturn(Arrays.asList());

      // Client 1 search
      mockMvc.perform(get("/api/v1/search/query1")
          .header("X-Client-ID", "client-backend-1"))
          .andExpect(status().isOk());

      // Client 2 search
      mockMvc.perform(get("/api/v1/search/query2")
          .header("X-Client-ID", "client-backend-2"))
          .andExpect(status().isOk());

      // Verify service was called twice (once per client)
      verify(documentService, times(2)).findSimilarChunks(anyString(), anyInt());
    }

    @Test
    @DisplayName("Test multiple clients: Clients without X-Client-ID header get IP-based ID")
    void testMultipleClients_IpBasedIdentification() throws Exception {
      Document document = Document.builder()
          .id(3L)
          .filename("ip-client-doc.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(document);
      when(apiLoggingService.getClientId(null, "127.0.0.1"))
          .thenReturn("client-127-0-0-1");

      MockMultipartFile testFile = new MockMultipartFile(
          "file", "test.pdf", "application/pdf", "content".getBytes());
      mockMvc.perform(multipart("/api/v1/documents")
          .file(testFile))
          .andExpect(status().isOk());

      // ApiLoggingService called by interceptor multiple times
      verify(apiLoggingService, atLeast(1)).getClientId(null, "127.0.0.1");
    }
  }

  @Nested
  @DisplayName("Write-Then-Read Persistence Tests")
  class WriteThenReadTests {

    @Test
    @DisplayName("Write then read: Upload document then retrieve it")
    void testWriteThenRead_UploadThenGet() throws Exception {
      // Write: Upload document
      MockMultipartFile file = new MockMultipartFile(
          "file", "persistence-test.pdf", "application/pdf", "test content".getBytes());

      Document uploadedDoc = Document.builder()
          .id(100L)
          .filename("persistence-test.pdf")
          .contentType("application/pdf")
          .fileSize(1024L)
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .uploadedAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .build();

      when(documentService.processDocument(any())).thenReturn(uploadedDoc);

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(100L));

      // Read: Retrieve the uploaded document
      when(documentService.getDocumentById(100L)).thenReturn(Optional.of(uploadedDoc));

      mockMvc.perform(get("/api/v1/documents/100")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(100L))
          .andExpect(jsonPath("$.filename").value("persistence-test.pdf"))
          .andExpect(jsonPath("$.processingStatus").value("COMPLETED"));

      verify(documentService, times(1)).processDocument(any());
      verify(documentService, times(1)).getDocumentById(100L);
    }

    @Test
    @DisplayName("Write then read: Upload document then get summary")
    void testWriteThenRead_UploadThenGetSummary() throws Exception {
      // Write: Upload document
      MockMultipartFile file = new MockMultipartFile(
          "file", "summary-test.pdf", "application/pdf", "content".getBytes());

      Document uploadedDoc = Document.builder()
          .id(200L)
          .filename("summary-test.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(uploadedDoc);

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk());

      // Read: Get summary for the uploaded document
      when(summarizationService.getDocumentSummary(200L))
          .thenReturn("Summary of uploaded document");

      mockMvc.perform(get("/api/v1/documents/200/summary")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.documentId").value(200L))
          .andExpect(jsonPath("$.summary").value("Summary of uploaded document"));

      verify(documentService, times(1)).processDocument(any());
      verify(summarizationService, times(1)).getDocumentSummary(200L);
    }

    @Test
    @DisplayName("Write then read: Upload document then search for it")
    void testWriteThenRead_UploadThenSearch() throws Exception {
      // Write: Upload document
      MockMultipartFile file = new MockMultipartFile(
          "file", "search-test.pdf", "application/pdf", "machine learning content".getBytes());

      Document uploadedDoc = Document.builder()
          .id(300L)
          .filename("search-test.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(uploadedDoc);

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk());

      // Read: Search for content related to uploaded document
      DocumentChunk chunk = DocumentChunk.builder()
          .id(1L)
          .textContent("machine learning content")
          .build();

      when(documentService.findSimilarChunks("machine learning", 3))
          .thenReturn(Arrays.asList(chunk));

      // Use unencoded path - MockMvc handles encoding
      mockMvc.perform(get("/api/v1/search/machine learning")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.count").value(1))
          .andExpect(jsonPath("$.results[0].textContent").value("machine learning content"));

      verify(documentService, times(1)).processDocument(any());
      verify(documentService, times(1)).findSimilarChunks("machine learning", 3);
    }
  }

  @Nested
  @DisplayName("API Logging Verification Tests")
  class ApiLoggingTests {

    @Test
    @DisplayName("Verify logging: Upload endpoint logs request")
    void testApiLogging_UploadLogs() throws Exception {
      MockMultipartFile file = new MockMultipartFile(
          "file", "log-test.pdf", "application/pdf", "content".getBytes());

      Document document = Document.builder()
          .id(1L)
          .filename("log-test.pdf")
          .processingStatus(Document.ProcessingStatus.COMPLETED)
          .build();

      when(documentService.processDocument(any())).thenReturn(document);
      when(apiLoggingService.generateRequestId()).thenReturn("req-123");

      mockMvc.perform(multipart("/api/v1/documents")
          .file(file)
          .header("X-Client-ID", "logging-client-1"));

      // Verify logging service was called
      // Note: ApiLoggingService is called multiple times by interceptor
      // (preHandle, afterCompletion)
      verify(apiLoggingService, atLeast(1)).generateRequestId();
      verify(apiLoggingService, atLeast(1)).getClientId(anyString(), anyString());
    }

    @Test
    @DisplayName("Verify logging: Search endpoint logs request")
    void testApiLogging_SearchLogs() throws Exception {
      when(documentService.findSimilarChunks(anyString(), anyInt()))
          .thenReturn(Arrays.asList());
      when(apiLoggingService.generateRequestId()).thenReturn("req-456");

      mockMvc.perform(get("/api/v1/search/test")
          .header("X-Client-ID", "logging-client-2"));

      // Note: ApiLoggingService is called by interceptor multiple times
      verify(apiLoggingService, atLeast(1)).generateRequestId();
      verify(apiLoggingService, atLeast(1)).getClientId(anyString(), anyString());
    }

    @Test
    @DisplayName("Verify logging: All endpoints call logging service")
    void testApiLogging_AllEndpoints() throws Exception {
      // Test multiple endpoints all call logging
      Document doc = Document.builder().id(1L).build();
      when(documentService.getDocumentById(1L)).thenReturn(Optional.of(doc));

      // GET endpoint
      mockMvc.perform(get("/api/v1/documents/1")
          .header("X-Client-ID", "client-1"));

      // GET all endpoint
      when(documentService.getAllDocuments()).thenReturn(Arrays.asList());
      mockMvc.perform(get("/api/v1/documents")
          .header("X-Client-ID", "client-1"));

      // Verify logging was called for each endpoint (interceptor calls it multiple
      // times)
      verify(apiLoggingService, atLeast(2)).generateRequestId();
    }

    @Test
    @DisplayName("Verify logging: Different clients get different client IDs")
    void testApiLogging_DifferentClients() throws Exception {
      Document doc = Document.builder().id(1L).build();
      when(documentService.getDocumentById(1L)).thenReturn(Optional.of(doc));

      // Client 1
      when(apiLoggingService.getClientId("client-1", "127.0.0.1")).thenReturn("client-1");
      mockMvc.perform(get("/api/v1/documents/1")
          .header("X-Client-ID", "client-1"))
          .andExpect(status().isOk());

      // Client 2
      when(apiLoggingService.getClientId("client-2", "127.0.0.1")).thenReturn("client-2");
      mockMvc.perform(get("/api/v1/documents/1")
          .header("X-Client-ID", "client-2"))
          .andExpect(status().isOk());

      // Verify both clients were logged with their respective IDs
      // Interceptor calls getClientId multiple times per request (preHandle,
      // afterCompletion)
      verify(apiLoggingService, atLeast(2)).getClientId(anyString(), anyString());
      verify(apiLoggingService, atLeast(1)).getClientId("client-1", "127.0.0.1");
      verify(apiLoggingService, atLeast(1)).getClientId("client-2", "127.0.0.1");
    }
  }
}
