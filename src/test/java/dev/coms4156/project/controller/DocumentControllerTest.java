package dev.coms4156.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import dev.coms4156.project.service.RagService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private DocumentApiController controller;

  @BeforeEach
  void setUp() {
    documentService = mock(DocumentService.class);
    summarizationService = mock(DocumentSummarizationService.class);
    ragService = mock(RagService.class);
    controller = new DocumentApiController(documentService, summarizationService, ragService);
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
    when(file.getOriginalFilename()).thenReturn("test.pdf");
    when(file.getSize()).thenReturn(1024L);
    when(file.getContentType()).thenReturn("application/pdf");

    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);

    when(documentService.processDocument(file)).thenReturn(document);

    // When
    ResponseEntity<?> response = controller.uploadDocument(file);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals(1L, responseBody.get("documentId"));
    assertEquals("test.pdf", responseBody.get("filename"));
    assertEquals(Document.ProcessingStatus.COMPLETED, responseBody.get("status"));
  }

  @Test
  void testUploadDocument_EmptyFile() throws Exception {
    // Given
    MultipartFile file = mock(MultipartFile.class);
    when(file.isEmpty()).thenReturn(true);
    when(documentService.processDocument(file)).thenThrow(
        new IllegalArgumentException("File is empty"));

    // When
    ResponseEntity<?> response = controller.uploadDocument(file);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<String, String> responseBody = (Map<String, String>) response.getBody();
    assertEquals("File is empty", responseBody.get("error"));
  }

  @Test
  void testGetDocument_Success() {
    // Given
    Long documentId = 1L;
    Document document = makeDoc(documentId, "test.pdf", "application/pdf", 1024L,
        Document.ProcessingStatus.COMPLETED, "Test summary"
    );

    when(documentService.getDocumentById(documentId)).thenReturn(Optional.of(document));

    // When
    ResponseEntity<?> response = controller.getDocument(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals(documentId, responseBody.get("id"));
    assertEquals("test.pdf", responseBody.get("filename"));
    assertEquals(Document.ProcessingStatus.COMPLETED, responseBody.get("processingStatus"));
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
  void testGetDocumentRelationships() {
    // Given
    Long documentId = 1L;

    // When
    ResponseEntity<?> response = controller.getDocumentRelationships(documentId);

    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals(documentId, responseBody.get("documentId"));
    assertEquals(0, responseBody.get("count"));
    assertEquals("Relationship analysis not yet implemented", responseBody.get("message"));
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
    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
    assertEquals(documentId, responseBody.get("documentId"));
    assertEquals(summary, responseBody.get("summary"));
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

  // @Test
  // void testSearchDocuments() {
  //     // Given
  //     String searchText = "machine learning";

  //     // When
  //     ResponseEntity<?> response = controller.searchDocuments(searchText);

  //     // Then
  //     assertEquals(HttpStatus.OK, response.getStatusCode());
  //     assertNotNull(response.getBody());
  //     Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
  //     assertEquals(searchText, responseBody.get("query"));
  //     assertEquals(0, responseBody.get("count"));
  //     assertEquals("Search functionality not yet implemented", responseBody.get("message"));
  // }
  @Test
  void testGetAllDocuments_Success() {
    List<Document> docs = List.of(
        makeDoc(1L, "a.pdf", "application/pdf", 1, Document.ProcessingStatus.UPLOADED, null),
        makeDoc(2L, "b.pdf", "application/pdf", 2, Document.ProcessingStatus.COMPLETED, "sum")
    );
    when(documentService.getAllDocuments()).thenReturn(docs);
    ResponseEntity<?> response = controller.getAllDocuments();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals(2, body.get("count"));
    assertEquals(docs, body.get("documents"));
    assertEquals("Documents retrieved", body.get("message"));
  }

  @Test
  void testGetProcessingStatistics_AllStatuses() {
    List<Document> all = List.of(makeDoc(1L, "a", "t", 1, Document.ProcessingStatus.UPLOADED, null),
        makeDoc(2L, "b", "t", 1, Document.ProcessingStatus.TEXT_EXTRACTED, null),
        makeDoc(3L, "c", "t", 1, Document.ProcessingStatus.CHUNKED, null),
        makeDoc(4L, "d", "t", 1, Document.ProcessingStatus.EMBEDDINGS_GENERATED, null),
        makeDoc(5L, "e", "t", 1, Document.ProcessingStatus.SUMMARIZED, null),
        makeDoc(6L, "f", "t", 1, Document.ProcessingStatus.COMPLETED, null),
        makeDoc(7L, "g", "t", 1, Document.ProcessingStatus.FAILED, null)
    );
    when(documentService.getAllDocuments()).thenReturn(all);

    ResponseEntity<?> response = controller.getProcessingStatistics();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals(7L, body.get("total"));
    Map<String, Long> byStatus = (Map<String, Long>) body.get("byStatus");
    assertEquals(1L, byStatus.get("UPLOADED"));
    assertEquals(1L, byStatus.get("TEXT_EXTRACTED"));
    assertEquals(1L, byStatus.get("CHUNKED"));
    assertEquals(1L, byStatus.get("EMBEDDINGS_GENERATED"));
    assertEquals(1L, byStatus.get("SUMMARIZED"));
    assertEquals(1L, byStatus.get("COMPLETED"));
    assertEquals(1L, byStatus.get("FAILED"));
    double completionRate = (double) body.get("completionRate");
    double failureRate = (double) body.get("failureRate");
    assertEquals(1.0 / 7.0, completionRate, 1e-9);
    assertEquals(1.0 / 7.0, failureRate, 1e-9);
  }

  @Test
  void testGetProcessingStatistics_Zero() {
    when(documentService.getAllDocuments()).thenReturn(List.of());
    ResponseEntity<?> response = controller.getProcessingStatistics();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals(0L, body.get("total"));
    assertEquals(0.0, (double) body.get("completionRate"));
    assertEquals(0.0, (double) body.get("failureRate"));
  }

  @Test
  void testDeleteDocument_Success() {
    final long id = 28L;
    Document existing = makeDoc(id, "x", "t", 1L, Document.ProcessingStatus.COMPLETED, null);
    Optional<Document> i = Optional.of(existing);
    when(documentService.getDocumentById(id)).thenReturn(i);
    doNothing().when(documentService).deleteDocument(id);

    ResponseEntity<?> response = controller.deleteDocument(28L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertEquals(28L, body.get("documentId"));
    assertEquals("Document deleted successfully", body.get("message"));
    verify(documentService).deleteDocument(28L);
  }

  @Test
  void testDeleteDocument_NotFound() {
    when(documentService.getDocumentById(77L)).thenReturn(Optional.empty());
    ResponseEntity<?> response = controller.deleteDocument(77L);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    Map<String, String> body = (Map<String, String>) response.getBody();
    assertEquals("Document not found", body.get("error"));
    verify(documentService, never()).deleteDocument(anyLong());
  }

  @Test
  void testGetDocumentsWithSummaries_EmptyList() {
    when(documentService.getAllDocuments()).thenReturn(java.util.List.of());
    ResponseEntity<Map<String, Object>> response = controller.getDocumentsWithSummaries();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertNotNull(body);

    assertEquals(0, body.get("count"));
    List<Document> docs = (List<Document>) body.get("documents");
    assertNotNull(docs);
    assertTrue(docs.isEmpty());
  }

  @Test
  void testGetDocumentsWithSummaries() {
    List<Document> all = new java.util.ArrayList<>();
    all.add(makeDoc(1L, "2", "t", 1L, Document.ProcessingStatus.SUMMARIZED, "hello"));
    all.add(makeDoc(2L, "8", "t", 1L, Document.ProcessingStatus.SUMMARIZED, "yes"));
    all.add(makeDoc(3L, "28", "t", 1L, Document.ProcessingStatus.SUMMARIZED, "282828"));
    all.add(makeDoc(4L, "a", "t", 1L, Document.ProcessingStatus.COMPLETED, null));
    when(documentService.getAllDocuments()).thenReturn(all);

    ResponseEntity<Map<String, Object>> response = controller.getDocumentsWithSummaries();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertNotNull(body);

    assertEquals(3, body.get("count"));
    List<Document> docs = (List<Document>) body.get("documents");
    assertEquals(3, docs.size());
    Set<Long> ids = new HashSet<>();
    for (Document doc : docs) {
      ids.add(doc.getId());
    }
    assertTrue(ids.contains(1L));
    assertTrue(ids.contains(3L));
    assertTrue(ids.contains(2L));
    assertFalse(ids.contains(4L));
  }


}