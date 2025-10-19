package dev.coms4156.project.controller;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentApiControllerTest {

    private DocumentService documentService;
    private DocumentSummarizationService summarizationService;
    private DocumentApiController controller;

    @BeforeEach
    void setUp() {
        documentService = mock(DocumentService.class);
        summarizationService = mock(DocumentSummarizationService.class);
        controller = new DocumentApiController(documentService, summarizationService);
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
        assertEquals("COMPLETED", responseBody.get("status"));
    }

    @Test
    void testUploadDocument_EmptyFile() throws Exception {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(documentService.processDocument(file)).thenThrow(new IllegalArgumentException("File is empty"));

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
        Document document = new Document();
        document.setId(documentId);
        document.setFilename("test.pdf");
        document.setContentType("application/pdf");
        document.setFileSize(1024L);
        document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
        document.setSummary("Test summary");

        when(documentService.getDocumentById(documentId)).thenReturn(Optional.of(document));

        // When
        ResponseEntity<?> response = controller.getDocument(documentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(documentId, responseBody.get("id"));
        assertEquals("test.pdf", responseBody.get("filename"));
        assertEquals("COMPLETED", responseBody.get("processingStatus"));
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

    @Test
    void testSearchDocuments() {
        // Given
        String searchText = "machine learning";

        // When
        ResponseEntity<?> response = controller.searchDocuments(searchText);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(searchText, responseBody.get("query"));
        assertEquals(0, responseBody.get("count"));
        assertEquals("Search functionality not yet implemented", responseBody.get("message"));
    }
}