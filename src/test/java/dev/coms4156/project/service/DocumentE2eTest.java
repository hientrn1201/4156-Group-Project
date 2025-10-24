package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import dev.coms4156.project.repository.DocumentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * End-to-end test for document processing workflow.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.SQL=WARN",
    "logging.level.dev.coms4156.project=INFO"
})
@Transactional
class DocumentE2eTest {

  @Autowired
  private DocumentService documentService;

  @Autowired
  private DocumentRepository documentRepository;

  @Autowired
  private DocumentChunkRepository documentChunkRepository;

  @MockBean
  private DocumentTextExtractionService textExtractionService;

  @MockBean
  private DocumentChunkingService chunkingService;

  @MockBean
  private SimpleEmbeddingService embeddingService;

  @MockBean
  private DocumentSummarizationService summarizationService;

  @Test
  void testDocumentWriteThenRead_CompleteCycle() throws Exception {
    // Given - Mock AI services to return predictable responses
    String mockExtractedText =
        "This is a test document about machine learning and artificial intelligence. "
            + "It contains multiple sentences "
            + "to test the chunking and summarization functionality. "
            + "The document discusses various AI techniques and "
            + "their applications in real-world scenarios.";

    byte[] mockContent = "Mock PDF content".getBytes();
    MultipartFile mockFile = new MockMultipartFile(
        "file",
        "test-document.pdf",
        "application/pdf",
        mockContent);

    // Mock text extraction service
    when(textExtractionService.detectContentType(mockFile)).thenReturn("application/pdf");
    when(textExtractionService.isSupportedContentType("application/pdf")).thenReturn(true);
    when(textExtractionService.extractText(mockFile)).thenReturn(mockExtractedText);

    // Mock chunking service
    DocumentChunk chunk1 = DocumentChunk.builder()
        .textContent("This is a test document about machine learning and artificial intelligence.")
        .build();
    DocumentChunk chunk2 = DocumentChunk.builder()
        .textContent(
            "It contains multiple sentences to test the chunking and summarization functionality.")
        .build();
    DocumentChunk chunk3 = DocumentChunk.builder()
        .textContent(
            "The document discusses various AI techniques and their "
                + "applications in real-world scenarios.")
        .build();

    List<DocumentChunk> mockChunks = Arrays.asList(chunk1, chunk2, chunk3);
    when(chunkingService.chunkDocument(any(Document.class))).thenReturn(mockChunks);

    // Mock embedding service
    when(embeddingService.generateEmbeddings(anyList())).thenReturn(mockChunks);

    // Note: DocumentService uses its own generateSummary() method, not
    // DocumentSummarizationService
    // The actual summary will be the first 200 characters of the extracted text
    final String expectedSummary =
        "This is a test document about machine learning and artificial intelligence. "
            + "It contains multiple sentences to "
            + "test the chunking and summarization functionality.";

    // When - Step 1: Upload and process document (WRITE)
    Document savedDocument = documentService.processDocument(mockFile);

    // Then - Verify document was saved with correct data
    assertNotNull(savedDocument);
    assertNotNull(savedDocument.getId());
    assertEquals("test-document.pdf", savedDocument.getFilename());
    assertEquals("application/pdf", savedDocument.getContentType());
    assertEquals((long) mockContent.length, savedDocument.getFileSize());
    assertEquals(mockExtractedText, savedDocument.getExtractedText());
    assertEquals(expectedSummary, savedDocument.getSummary());
    assertEquals(Document.ProcessingStatus.COMPLETED,
        savedDocument.getProcessingStatus());

    // Note: DocumentService.processDocument() creates chunks but doesn't save them
    // to database
    // The chunks are only used for embedding generation, not persisted
    List<DocumentChunk> savedChunks =
        documentChunkRepository.findByDocumentId(savedDocument.getId());
    assertEquals(0,
        savedChunks.size()); // No chunks are saved to database in current implementation

    // When - Step 2: Retrieve the document (READ)
    Optional<Document> retrievedDocument = documentService.getDocumentById(savedDocument.getId());

    // Then - Verify retrieved data matches saved data
    assertTrue(retrievedDocument.isPresent());
    Document doc = retrievedDocument.get();

    // Verify all fields match
    assertEquals(savedDocument.getId(), doc.getId());
    assertEquals(savedDocument.getFilename(), doc.getFilename());
    assertEquals(savedDocument.getContentType(), doc.getContentType());
    assertEquals(savedDocument.getFileSize(), doc.getFileSize());
    assertEquals(savedDocument.getExtractedText(), doc.getExtractedText());
    assertEquals(savedDocument.getSummary(), doc.getSummary());
    assertEquals(savedDocument.getProcessingStatus(), doc.getProcessingStatus());
    assertEquals(savedDocument.getUploadedAt(), doc.getUploadedAt());
    assertEquals(savedDocument.getUpdatedAt(), doc.getUpdatedAt());

    // Verify AI services were called correctly
    verify(textExtractionService).detectContentType(mockFile);
    verify(textExtractionService).isSupportedContentType("application/pdf");
    verify(textExtractionService).extractText(mockFile);
    verify(chunkingService).chunkDocument(any(Document.class));
    verify(embeddingService).generateEmbeddings(anyList());
  }

  @Test
  void testDocumentWriteThenRead_MultipleDocuments() throws Exception {
    // Given - Create multiple documents
    String[] filenames = {"doc1.pdf", "doc2.pdf", "doc3.pdf"};
    String[] extractedTexts = {
        "First document about AI and machine learning.",
        "Second document about data science and analytics.",
        "Third document about natural language processing."
    };

    // Mock services for multiple documents
    for (int i = 0; i < filenames.length; i++) {
      MultipartFile mockFile = new MockMultipartFile(
          "file", filenames[i], "application/pdf", "Mock content".getBytes());

      when(textExtractionService.detectContentType(mockFile)).thenReturn("application/pdf");
      when(textExtractionService.isSupportedContentType("application/pdf")).thenReturn(true);
      when(textExtractionService.extractText(mockFile)).thenReturn(extractedTexts[i]);

      DocumentChunk mockChunk = DocumentChunk.builder()
          .textContent(extractedTexts[i])
          .build();
      when(chunkingService.chunkDocument(any(Document.class))).thenReturn(Arrays.asList(mockChunk));
      when(embeddingService.generateEmbeddings(anyList())).thenReturn(Arrays.asList(mockChunk));
      when(summarizationService.generateSummary(any(Document.class)))
          .thenReturn("Summary for " + filenames[i]);

      // When - Upload document
      Document savedDoc = documentService.processDocument(mockFile);

      // Then - Verify it was saved
      assertNotNull(savedDoc.getId());
      assertEquals(filenames[i], savedDoc.getFilename());
      assertEquals(extractedTexts[i], savedDoc.getExtractedText());

      // When - Retrieve document
      Optional<Document> retrievedDoc = documentService.getDocumentById(savedDoc.getId());

      // Then - Verify retrieved data matches
      assertTrue(retrievedDoc.isPresent());
      assertEquals(savedDoc.getId(), retrievedDoc.get().getId());
      assertEquals(savedDoc.getFilename(), retrievedDoc.get().getFilename());
      assertEquals(savedDoc.getExtractedText(), retrievedDoc.get().getExtractedText());
    }

    // Verify all documents are retrievable via getAllDocuments
    List<Document> allDocuments = documentService.getAllDocuments();
    assertEquals(3, allDocuments.size());

    // Verify we can find documents by filename
    List<Document> docsByFilename = documentService.getDocumentsByFilename("doc");
    assertEquals(3, docsByFilename.size());
  }

  @Test
  void testDocumentWriteThenRead_NotFoundScenario() throws Exception {
    // Given - Non-existent document ID
    Long nonExistentId = 99999L;

    // When - Try to retrieve non-existent document
    Optional<Document> result = documentService.getDocumentById(nonExistentId);

    // Then - Should return empty
    assertFalse(result.isPresent());
  }

  @Test
  void testDocumentWriteThenRead_StatusFiltering() throws Exception {
    // Given - Create documents with different statuses
    MultipartFile mockFile = new MockMultipartFile(
        "file", "test.pdf", "application/pdf", "Mock content".getBytes());

    when(textExtractionService.detectContentType(mockFile)).thenReturn("application/pdf");
    when(textExtractionService.isSupportedContentType("application/pdf")).thenReturn(true);
    when(textExtractionService.extractText(mockFile)).thenReturn("Test content");

    DocumentChunk mockChunk = DocumentChunk.builder()
        .textContent("Test content")
        .build();
    when(chunkingService.chunkDocument(any(Document.class))).thenReturn(Arrays.asList(mockChunk));
    when(embeddingService.generateEmbeddings(anyList())).thenReturn(Arrays.asList(mockChunk));
    when(summarizationService.generateSummary(any(Document.class))).thenReturn("Test summary");

    // When - Upload document (should be COMPLETED after processing)
    Document savedDoc = documentService.processDocument(mockFile);

    // Then - Verify status filtering works
    List<Document> completedDocs =
        documentService.getDocumentsByStatus(Document.ProcessingStatus.COMPLETED);
    assertTrue(completedDocs.stream().anyMatch(doc -> doc.getId().equals(savedDoc.getId())));

    List<Document> uploadedDocs =
        documentService.getDocumentsByStatus(Document.ProcessingStatus.UPLOADED);
    assertFalse(uploadedDocs.stream().anyMatch(doc -> doc.getId().equals(savedDoc.getId())));
  }
}
