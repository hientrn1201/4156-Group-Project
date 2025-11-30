package dev.coms4156.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.dtos.DocumentDTO;
import dev.coms4156.project.dtos.DocumentListResponse;
import dev.coms4156.project.dtos.DocumentRelationshipInfoResponse;
import dev.coms4156.project.dtos.DocumentSearchResponse;
import dev.coms4156.project.dtos.DocumentStatsResponse;
import dev.coms4156.project.dtos.DocumentStatusCounts;
import dev.coms4156.project.dtos.DocumentSummaryResponse;
import dev.coms4156.project.dtos.DocumentUploadResponse;
import dev.coms4156.project.dtos.ErrorResponse;
import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.service.ApiLoggingService;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import dev.coms4156.project.service.RagService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

class DocumentControllerTest {

  private DocumentService documentService;
  private DocumentSummarizationService summarizationService;
  private RagService ragService;
  private ApiLoggingService apiLoggingService;
  private DocumentApiController controller;

  @BeforeEach
  void setUp() throws Exception {
    documentService = mock(DocumentService.class);
    summarizationService = mock(DocumentSummarizationService.class);
    ragService = mock(RagService.class);
    apiLoggingService = mock(ApiLoggingService.class);
    controller = new DocumentApiController(documentService, summarizationService, ragService);

    // Inject the mocked ApiLoggingService using reflection
    Field apiLoggingServiceField =
        DocumentApiController.class.getDeclaredField("apiLoggingService");
    apiLoggingServiceField.setAccessible(true);
    apiLoggingServiceField.set(controller, apiLoggingService);

    // Mock the ApiLoggingService methods
    when(apiLoggingService.generateRequestId()).thenReturn("test-request-id");
    when(apiLoggingService.getClientId(any(), any())).thenReturn("test-client-id");
  }

  private Document makeDoc(Long id, String filename, String contentType, long size,
                           Document.ProcessingStatus status, String summary) {
    Document d = new Document();
    d.setId(id);
    d.setFilename(filename);
    d.setContentType(contentType);
    d.setFileSize(size);
    d.setProcessingStatus(status);
    d.setSummary(summary);
    d.setUploadedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
    d.setUpdatedAt(LocalDateTime.of(2024, 1, 2, 0, 0));
    return d;
  }

  @Test
  void testUploadDocument_Success() throws Exception {
    // Given
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(file.getOriginalFilename()).thenReturn("test.pdf");
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("application/pdf");
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);

    when(documentService.processDocument(file)).thenReturn(document);

    // When
    ResponseEntity<?> response = controller.uploadDocument(file, request);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentUploadResponse responseBody = (DocumentUploadResponse) response.getBody();
    assertEquals(1L, responseBody.getDocumentId());
    assertEquals("test.pdf", responseBody.getFilename());
    assertEquals(Document.ProcessingStatus.COMPLETED, responseBody.getProcessingStatus());
  }

  @Test
  void testUploadDocument_EmptyFile() throws Exception {
    // Given
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(file.isEmpty()).thenReturn(true);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.processDocument(file)).thenThrow(
        new IllegalArgumentException("File is empty"));

    // When
    ResponseEntity<?> response = controller.uploadDocument(file, request);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse responseBody = (ErrorResponse) response.getBody();
    assertEquals("File is empty", responseBody.getError());
  }

  @Test
  void testUploadDocument_ServiceException() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.processDocument(file)).thenThrow(
        new RuntimeException("Processing failed"));

    ResponseEntity<?> response = controller.uploadDocument(file, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetDocument_Success() {
    // Given
    Long documentId = 1L;
    Document document = makeDoc(documentId, "test.pdf", "application/pdf",
        1024L, Document.ProcessingStatus.COMPLETED, "Test summary");

    when(documentService.getDocumentById(documentId)).thenReturn(Optional.of(document));

    // When
    ResponseEntity<?> response = controller.getDocument(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentDTO responseBody = (DocumentDTO) response.getBody();
    assertEquals(documentId, responseBody.getId());
    assertEquals("test.pdf", responseBody.getFilename());
    assertEquals(Document.ProcessingStatus.COMPLETED, responseBody.getProcessingStatus());
  }

  @Test
  void testGetDocument_NotFound() {
    // Given
    Long documentId = 999L;
    when(documentService.getDocumentById(documentId)).thenReturn(Optional.empty());

    // When
    ResponseEntity<?> response = controller.getDocument(documentId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetDocument_ServiceException() {
    when(documentService.getDocumentById(1L)).thenThrow(new RuntimeException("Database error"));
    ResponseEntity<?> response = controller.getDocument(1L);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetDocumentRelationships() {
    // Given
    Long documentId = 1L;

    // When
    ResponseEntity<?> response = controller.getDocumentRelationships(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentRelationshipInfoResponse responseBody =
        (DocumentRelationshipInfoResponse) response.getBody();
    assertEquals(documentId, responseBody.getDocumentId());
    assertEquals(0, responseBody.getCount());
    assertEquals("Relationship analysis not yet implemented", responseBody.getMessage());
  }

  @Test
  void testGetDocumentRelationships_LargeId() {
    ResponseEntity<?> response = controller.getDocumentRelationships(999999L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetDocumentRelationships_NullId() {
    ResponseEntity<?> response = controller.getDocumentRelationships(null);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetDocumentSummary_Success() {
    // Given
    Long documentId = 1L;
    String summary = "This is a test summary";
    when(summarizationService.getDocumentSummary(documentId)).thenReturn(summary);

    // When
    ResponseEntity<?> response = controller.getDocumentSummary(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentSummaryResponse responseBody = (DocumentSummaryResponse) response.getBody();
    assertEquals(documentId, responseBody.getDocumentId());
    assertEquals(summary, responseBody.getSummary());
  }

  @Test
  void testGetDocumentSummary_NotFound() {
    // Given
    Long documentId = 1L;
    when(summarizationService.getDocumentSummary(documentId)).thenReturn(null);

    // When
    ResponseEntity<?> response = controller.getDocumentSummary(documentId);

    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testGetDocumentSummary_ServiceException() {
    when(summarizationService.getDocumentSummary(
        1L)).thenThrow(new RuntimeException("Service error"));
    ResponseEntity<?> response = controller.getDocumentSummary(1L);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testSearchDocuments_Success() {
    // Given
    String searchText = "machine learning";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Machine learning algorithms")
        .build();
    when(documentService.findSimilarChunks(searchText, 3)).thenReturn(List.of(chunk));

    // When
    ResponseEntity<?> response = controller.searchDocuments(searchText, request);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentSearchResponse responseBody = (DocumentSearchResponse) response.getBody();
    assertEquals(searchText, responseBody.getQuery());
    assertEquals(1, responseBody.getCount());
    assertEquals("Search completed successfully", responseBody.getMessage());
    assertNotNull(responseBody.getResults());
  }

  @Test
  void testSearchDocuments_EmptyQuery() {
    // Given
    String searchText = "";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks(searchText, 3)).thenReturn(List.of());

    // When
    ResponseEntity<?> response = controller.searchDocuments(searchText, request);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    DocumentSearchResponse responseBody = (DocumentSearchResponse) response.getBody();
    assertEquals(searchText, responseBody.getQuery());
    assertEquals(0, responseBody.getCount());
    assertEquals("Search completed successfully", responseBody.getMessage());
  }

  @Test
  void testSearchDocuments_ServiceException() {
    // Given
    String searchText = "test query";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks(searchText, 3))
        .thenThrow(new RuntimeException("Search error"));

    // When
    ResponseEntity<?> response = controller.searchDocuments(searchText, request);

    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    ErrorResponse responseBody = (ErrorResponse) response.getBody();
    assertEquals("Search failed: Search error", responseBody.getError());
  }

  @Test
  void testGetAllDocuments_Success() {
    List<Document> docs = List.of(
        makeDoc(1L, "a.pdf", "application/pdf", 1,
            Document.ProcessingStatus.UPLOADED, null),
        makeDoc(2L, "b.pdf", "application/pdf", 2,
            Document.ProcessingStatus.COMPLETED, "sum"));
    when(documentService.getAllDocuments()).thenReturn(docs);
    ResponseEntity<?> response = controller.getAllDocuments("");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentListResponse body = (DocumentListResponse) response.getBody();
    assertEquals(2L, body.getCount());
    assertEquals(2, body.getDocuments().size());
  }

  @Test
  void testGetDocumentsByFilename_Success() {
    List<Document> docs = List.of(
        makeDoc(1L, "doc1.pdf", "application/pdf", 1,
            Document.ProcessingStatus.UPLOADED, null),
        makeDoc(2L, "doc2.pdf", "application/pdf", 2,
            Document.ProcessingStatus.UPLOADED, null));
    String queryString = "doc";
    when(documentService.getDocumentsByFilename(queryString)).thenReturn(docs);
    ResponseEntity<?> response = controller.getAllDocuments(queryString);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentListResponse body = (DocumentListResponse) response.getBody();
    assertEquals(2L, body.getCount());
    assertEquals(2, body.getDocuments().size());
  }

  @Test
  void testGetAllDocuments_ServiceException() {
    when(documentService.getAllDocuments()).thenThrow(new RuntimeException("Database error"));
    ResponseEntity<?> response = controller.getAllDocuments("");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void testGetProcessingStatistics_AllStatuses() {
    List<Document> all = List.of(makeDoc(1L, "a", "t", 1,
            Document.ProcessingStatus.UPLOADED, null),
        makeDoc(2L, "b", "t", 1,
            Document.ProcessingStatus.TEXT_EXTRACTED, null),
        makeDoc(3L, "c", "t", 1,
            Document.ProcessingStatus.CHUNKED, null),
        makeDoc(4L, "d", "t", 1,
            Document.ProcessingStatus.EMBEDDINGS_GENERATED, null),
        makeDoc(5L, "e", "t", 1,
            Document.ProcessingStatus.SUMMARIZED, null),
        makeDoc(6L, "f", "t", 1,
            Document.ProcessingStatus.COMPLETED, null),
        makeDoc(7L, "g", "t", 1,
            Document.ProcessingStatus.FAILED, null));
    when(documentService.getAllDocuments()).thenReturn(all);

    ResponseEntity<?> response = controller.getProcessingStatistics();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentStatsResponse body = (DocumentStatsResponse) response.getBody();
    assertEquals(7L, body.getTotal());
    DocumentStatusCounts byStatus = body.getByStatus();
    assertEquals(1L, byStatus.getUploaded());
    assertEquals(1L, byStatus.getTextExtracted());
    assertEquals(1L, byStatus.getChunked());
    assertEquals(1L, byStatus.getEmbeddingsGenerated());
    assertEquals(1L, byStatus.getSummarized());
    assertEquals(1L, byStatus.getCompleted());
    assertEquals(1L, byStatus.getFailed());
    double completionRate = body.getCompletionRate();
    double failureRate = body.getFailureRate();
    assertEquals(1.0 / 7.0, completionRate, 1e-9);
    assertEquals(1.0 / 7.0, failureRate, 1e-9);
  }

  @Test
  void testGetProcessingStatistics_Zero() {
    when(documentService.getAllDocuments()).thenReturn(List.of());
    ResponseEntity<?> response = controller.getProcessingStatistics();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentStatsResponse body = (DocumentStatsResponse) response.getBody();
    assertEquals(0L, body.getTotal());
    assertEquals(0.0, body.getCompletionRate());
    assertEquals(0.0, body.getFailureRate());
  }

  @Test
  void testGetProcessingStatistics_ServiceException() {
    when(documentService.getAllDocuments()).thenThrow(new RuntimeException("Database error"));
    try {
      controller.getProcessingStatistics();
    } catch (RuntimeException e) {
      assertEquals("Database error", e.getMessage());
    }
  }

  @Test
  void testDeleteDocument_Success() {
    final long id = 28L;
    Document existing = makeDoc(id, "x", "t", 1L,
        Document.ProcessingStatus.COMPLETED, null);
    Optional<Document> i = Optional.of(existing);
    when(documentService.getDocumentById(id)).thenReturn(i);
    doNothing().when(documentService).deleteDocument(id);

    ResponseEntity<?> response = controller.deleteDocument(28L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    // Note: Delete response still uses Map - could be converted to DTO in future
    assertNotNull(response.getBody());
    verify(documentService).deleteDocument(28L);
  }

  @Test
  void testDeleteDocument_NotFound() {
    when(documentService.getDocumentById(77L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.deleteDocument(77L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    // Note: Error response still uses Map - could be converted to ErrorResponse DTO
    assertNotNull(response.getBody());
    verify(documentService, never()).deleteDocument(anyLong());
  }

  @Test
  void testDeleteDocument_ServiceException() {
    when(documentService.getDocumentById(1L)).thenReturn(Optional.of(new Document()));
    doNothing().when(documentService).deleteDocument(1L);
    ResponseEntity<?> response = controller.deleteDocument(1L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testGetDocumentsWithSummaries_EmptyList() {
    when(documentService.getAllDocuments()).thenReturn(java.util.List.of());
    ResponseEntity<?> response = controller.getDocumentsWithSummaries();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentListResponse body = (DocumentListResponse) response.getBody();
    assertNotNull(body);

    assertEquals(0L, body.getCount());
    assertNotNull(body.getDocuments());
    assertTrue(body.getDocuments().isEmpty());
  }

  @Test
  void testGetDocumentsWithSummaries() {
    List<Document> all = new java.util.ArrayList<>();
    all.add(makeDoc(1L, "2", "t", 1L,
        Document.ProcessingStatus.SUMMARIZED, "hello"));
    all.add(makeDoc(2L, "8", "t", 1L,
        Document.ProcessingStatus.SUMMARIZED, "yes"));
    all.add(makeDoc(3L, "28", "t", 1L,
        Document.ProcessingStatus.SUMMARIZED, "282828"));
    all.add(makeDoc(4L, "a", "t", 1L,
        Document.ProcessingStatus.COMPLETED, null));
    when(documentService.getAllDocuments()).thenReturn(all);

    ResponseEntity<?> response = controller.getDocumentsWithSummaries();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentListResponse body = (DocumentListResponse) response.getBody();
    assertNotNull(body);

    assertEquals(3L, body.getCount());
    assertEquals(3, body.getDocuments().size());
    Set<Long> ids = new HashSet<>();
    for (DocumentDTO doc : body.getDocuments()) {
      ids.add(doc.getId());
    }
    assertTrue(ids.contains(1L));
    assertTrue(ids.contains(3L));
    assertTrue(ids.contains(2L));
    assertFalse(ids.contains(4L));
  }

  @Test
  void testGetDocumentsWithSummaries_ServiceException() {
    when(documentService.getAllDocuments()).thenThrow(new RuntimeException("Database error"));
    try {
      controller.getDocumentsWithSummaries();
    } catch (RuntimeException e) {
      assertEquals("Database error", e.getMessage());
    }
  }

  // ===== ADDITIONAL BOUNDARY ANALYSIS AND EQUIVALENCE PARTITION TESTS =====

  // Boundary analysis for upload - file size limits
  @Test
  void testUploadDocument_LargeFile() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(file.getOriginalFilename()).thenReturn("large.pdf");
    when(file.getSize()).thenReturn(50L * 1024 * 1024); // 50MB
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    when(documentService.processDocument(file))
        .thenThrow(new IllegalArgumentException("File too large"));

    ResponseEntity<?> response = controller.uploadDocument(file, request);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Invalid equivalence partition - unsupported file type
  @Test
  void testUploadDocument_UnsupportedFileType() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(file.getOriginalFilename()).thenReturn("image.jpg");
    when(file.getContentType()).thenReturn("image/jpeg");
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    when(documentService.processDocument(file))
        .thenThrow(new IllegalArgumentException("Unsupported file type"));

    ResponseEntity<?> response = controller.uploadDocument(file, request);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Boundary analysis - null filename
  @Test
  void testUploadDocument_NullFilename() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(file.getOriginalFilename()).thenReturn(null);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    when(documentService.processDocument(file))
        .thenThrow(new IllegalArgumentException("Filename cannot be null"));

    ResponseEntity<?> response = controller.uploadDocument(file, request);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Boundary analysis for document ID - negative ID
  @Test
  void testGetDocument_NegativeId() {
    when(documentService.getDocumentById(-1L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.getDocument(-1L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for document ID - zero ID
  @Test
  void testGetDocument_ZeroId() {
    when(documentService.getDocumentById(0L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.getDocument(0L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for document ID - maximum long value
  @Test
  void testGetDocument_MaxLongId() {
    when(documentService.getDocumentById(Long.MAX_VALUE)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.getDocument(Long.MAX_VALUE);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for search - empty search text
  @Test
  void testSearchDocuments_EmptyText() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks("", 3)).thenReturn(List.of());

    ResponseEntity<?> response = controller.searchDocuments("", request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for search - very long search text
  @Test
  void testSearchDocuments_VeryLongText() {
    String longText = "search ".repeat(1000);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks(longText, 3)).thenReturn(List.of());

    ResponseEntity<?> response = controller.searchDocuments(longText, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for search - special characters
  @Test
  void testSearchDocuments_SpecialCharacters() {
    String specialText = "@#$%^&*()_+-=[]{}|;':,.<>?/~`";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks(specialText, 3)).thenReturn(List.of());

    ResponseEntity<?> response = controller.searchDocuments(specialText, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for search - unicode characters
  @Test
  void testSearchDocuments_UnicodeCharacters() {
    String unicodeText = "机器学习 русский язык";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Client-ID")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(documentService.findSimilarChunks(unicodeText, 3)).thenReturn(List.of());

    ResponseEntity<?> response = controller.searchDocuments(unicodeText, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for filename filter - null filename
  @Test
  void testGetAllDocuments_NullFilename() {
    when(documentService.getAllDocuments()).thenReturn(List.of());
    ResponseEntity<?> response = controller.getAllDocuments(null);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for filename filter - whitespace-only filename
  @Test
  void testGetAllDocuments_WhitespaceFilename() {
    when(documentService.getAllDocuments()).thenReturn(List.of());
    ResponseEntity<?> response = controller.getAllDocuments("   ");
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for filename filter - very long filename
  @Test
  void testGetAllDocuments_VeryLongFilename() {
    String longFilename = "a".repeat(1000) + ".pdf";
    when(documentService.getDocumentsByFilename(longFilename)).thenReturn(List.of());
    ResponseEntity<?> response = controller.getAllDocuments(longFilename);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for delete - negative ID
  @Test
  void testDeleteDocument_NegativeId() {
    when(documentService.getDocumentById(-1L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.deleteDocument(-1L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for delete - zero ID
  @Test
  void testDeleteDocument_ZeroId() {
    when(documentService.getDocumentById(0L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.deleteDocument(0L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for summary - negative ID
  @Test
  void testGetDocumentSummary_NegativeId() {
    when(summarizationService.getDocumentSummary(-1L)).thenReturn(null);
    ResponseEntity<?> response = controller.getDocumentSummary(-1L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Boundary analysis for summary - zero ID
  @Test
  void testGetDocumentSummary_ZeroId() {
    when(summarizationService.getDocumentSummary(0L)).thenReturn(null);
    ResponseEntity<?> response = controller.getDocumentSummary(0L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // Valid equivalence partition - summary with empty string
  @Test
  void testGetDocumentSummary_EmptyString() {
    when(summarizationService.getDocumentSummary(1L)).thenReturn("");
    ResponseEntity<?> response = controller.getDocumentSummary(1L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  // Boundary analysis for relationships - negative ID
  @Test
  void testGetDocumentRelationships_NegativeId() {
    ResponseEntity<?> response = controller.getDocumentRelationships(-1L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentRelationshipInfoResponse responseBody = (DocumentRelationshipInfoResponse) response.getBody();
    assertEquals(-1L, responseBody.getDocumentId());
  }

  // Boundary analysis for relationships - zero ID
  @Test
  void testGetDocumentRelationships_ZeroId() {
    ResponseEntity<?> response = controller.getDocumentRelationships(0L);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentRelationshipInfoResponse responseBody = (DocumentRelationshipInfoResponse) response.getBody();
    assertEquals(0L, responseBody.getDocumentId());
  }

  // Valid equivalence partition - documents with blank summaries
  @Test
  void testGetDocumentsWithSummaries_BlankSummaries() {
    List<Document> all = List.of(
        makeDoc(1L, "doc1", "t", 1L, Document.ProcessingStatus.COMPLETED, "valid summary"),
        makeDoc(2L, "doc2", "t", 1L, Document.ProcessingStatus.COMPLETED, ""),
        makeDoc(3L, "doc3", "t", 1L, Document.ProcessingStatus.COMPLETED, "   "),
        makeDoc(4L, "doc4", "t", 1L, Document.ProcessingStatus.COMPLETED, null)
    );
    when(documentService.getAllDocuments()).thenReturn(all);

    ResponseEntity<?> response = controller.getDocumentsWithSummaries();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    DocumentListResponse body = (DocumentListResponse) response.getBody();
    assertEquals(1L, body.getCount()); // Only doc1 has non-blank summary
  }

}