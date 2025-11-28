package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.model.DocumentRelationship;
import dev.coms4156.project.repository.DocumentChunkRepository;
import dev.coms4156.project.repository.DocumentRelationshipRepository;
import dev.coms4156.project.repository.DocumentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock
  private DocumentRepository documentRepository;

  @Mock
  private DocumentChunkRepository documentChunkRepository;

  @Mock
  private DocumentRelationshipRepository documentRelationshipRepository;

  @Mock
  private DocumentTextExtractionService textExtractionService;

  @Mock
  private DocumentChunkingService chunkingService;

  @Mock
  private SimpleEmbeddingService embeddingService;

  @Mock
  private MultipartFile multipartFile;

  private DocumentService documentService;

  @BeforeEach
  void setUp() {
    documentService = new DocumentService(
        documentRepository,
        documentChunkRepository,
        documentRelationshipRepository,
        textExtractionService,
        chunkingService,
        embeddingService);
  }

  @Test
  void testProcessDocument_Success() throws Exception {
    // Given
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
    when(multipartFile.getSize()).thenReturn(1024L);
    when(textExtractionService.detectContentType(multipartFile)).thenReturn("application/pdf");
    when(textExtractionService.isSupportedContentType("application/pdf")).thenReturn(true);
    when(textExtractionService.extractText(multipartFile)).thenReturn("Sample text content");

    Document savedDocument = new Document();
    savedDocument.setId(1L);
    savedDocument.setFilename("test.pdf");
    savedDocument.setProcessingStatus(Document.ProcessingStatus.UPLOADED);

    when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Sample text content")
        .build();
    when(chunkingService.chunkDocument(any(Document.class))).thenReturn(Arrays.asList(chunk));
    when(embeddingService.generateEmbeddings(anyList())).thenReturn(Arrays.asList(chunk));

    // When
    Document result = documentService.processDocument(multipartFile);

    // Then
    assertNotNull(result);
    assertEquals("test.pdf", result.getFilename());
    verify(documentRepository, times(5)).save(any(Document.class));
  }

  @Test
  void testProcessDocument_UnsupportedFileType() throws Exception {
    // Given
    when(multipartFile.isEmpty()).thenReturn(false);
    when(textExtractionService.detectContentType(multipartFile)).thenReturn("application/unknown");
    when(textExtractionService.isSupportedContentType("application/unknown")).thenReturn(false);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      documentService.processDocument(multipartFile);
    });
  }

  @Test
  void testGetDocumentById_Found() {
    // Given
    Document document = new Document();
    document.setId(1L);
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

    // When
    Optional<Document> result = documentService.getDocumentById(1L);

    // Then
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
  }

  @Test
  void testGetAllDocuments() {
    // Given
    Document doc1 = new Document();
    doc1.setId(1L);
    Document doc2 = new Document();
    doc2.setId(2L);
    List<Document> documents = Arrays.asList(doc1, doc2);
    when(documentRepository.findAll()).thenReturn(documents);

    // When
    List<Document> result = documentService.getAllDocuments();

    // Then
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
  }

  @Test
  void testGetDocumentsByStatus() {
    // Given
    Document doc = new Document();
    doc.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    when(documentRepository.findByProcessingStatus(Document.ProcessingStatus.COMPLETED))
        .thenReturn(Arrays.asList(doc));

    // When
    List<Document> result =
        documentService.getDocumentsByStatus(Document.ProcessingStatus.COMPLETED);

    // Then
    assertEquals(1, result.size());
    assertEquals(Document.ProcessingStatus.COMPLETED, result.get(0).getProcessingStatus());
  }

  @Test
  void testDeleteDocument() {
    // Given
    Document document = new Document();
    document.setId(1L);
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(documentChunkRepository.findByDocumentId(1L)).thenReturn(Arrays.asList());
    when(documentRelationshipRepository.findByDocumentId(1L)).thenReturn(Arrays.asList());
    doNothing().when(documentRelationshipRepository).deleteAll(any());
    doNothing().when(documentChunkRepository).deleteAll(any());
    doNothing().when(documentRepository).delete(any(Document.class));

    // When
    documentService.deleteDocument(1L);

    // Then
    verify(documentRepository).findById(1L);
    verify(documentRelationshipRepository).findByDocumentId(1L);
    verify(documentChunkRepository).findByDocumentId(1L);
    verify(documentRepository).delete(document);
  }

  @Test
  void testDeleteDocument_WithChunksAndRelationships() {
    // Given
    Document document = new Document();
    document.setId(1L);
    DocumentChunk chunk1 = DocumentChunk.builder().id(1L).build();
    DocumentChunk chunk2 = DocumentChunk.builder().id(2L).build();
    DocumentRelationship relationship = DocumentRelationship.builder().id(1L).build();

    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(documentChunkRepository.findByDocumentId(1L))
        .thenReturn(Arrays.asList(chunk1, chunk2));
    when(documentRelationshipRepository.findByDocumentId(1L))
        .thenReturn(Arrays.asList(relationship));
    doNothing().when(documentRelationshipRepository).deleteAll(any());
    doNothing().when(documentChunkRepository).deleteAll(any());
    doNothing().when(documentRepository).delete(any(Document.class));

    // When
    documentService.deleteDocument(1L);

    // Then
    verify(documentRepository).findById(1L);
    verify(documentRelationshipRepository).findByDocumentId(1L);
    verify(documentRelationshipRepository).deleteAll(Arrays.asList(relationship));
    verify(documentChunkRepository).findByDocumentId(1L);
    verify(documentChunkRepository).deleteAll(Arrays.asList(chunk1, chunk2));
    verify(documentRepository).delete(document);
  }

  @Test
  void testDeleteDocument_NotFound() {
    // Given
    when(documentRepository.findById(1L)).thenReturn(Optional.empty());

    // When
    documentService.deleteDocument(1L);

    // Then
    verify(documentRepository).findById(1L);
    verify(documentRepository, times(0)).delete(any());
  }

  @Test
  void testGetDocumentChunks() {
    // Given
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Sample chunk")
        .build();
    when(chunkingService.getChunksForDocument(1L)).thenReturn(Arrays.asList(chunk));

    // When
    List<DocumentChunk> result = documentService.getDocumentChunks(1L);

    // Then
    assertEquals(1, result.size());
    assertEquals("Sample chunk", result.get(0).getTextContent());
  }

  @Test
  void testGetChunkStatistics() {
    // Given
    Document document = new Document();
    document.setId(1L);
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

    DocumentChunkingService.ChunkStatistics stats = new DocumentChunkingService.ChunkStatistics(
        5, 1000, 200, 150, 250);
    when(chunkingService.getChunkStatistics(document)).thenReturn(stats);

    // When
    DocumentChunkingService.ChunkStatistics result = documentService.getChunkStatistics(1L);

    // Then
    assertEquals(5, result.getTotalChunks());
    assertEquals(1000, result.getTotalCharacters());
  }

  @Test
  void testGenerateEmbeddingsForDocument() {
    // Given
    when(embeddingService.generateEmbeddingsForDocument(1L)).thenReturn(5);

    // When
    int result = documentService.generateEmbeddingsForDocument(1L);

    // Then
    assertEquals(5, result);
  }

  @Test
  void testFindSimilarChunks() {
    // Given
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Similar content")
        .build();
    when(embeddingService.findSimilarChunks("query", 3)).thenReturn(Arrays.asList(chunk));

    // When
    List<DocumentChunk> result = documentService.findSimilarChunks("query", 3);

    // Then
    assertEquals(1, result.size());
    assertEquals("Similar content", result.get(0).getTextContent());
  }

  @Test
  void testProcessDocument_EmptyFile() {
    when(multipartFile.isEmpty()).thenReturn(true);

    assertThrows(IllegalArgumentException.class,
        () -> documentService.processDocument(multipartFile));
  }

  @Test
  void testGetDocumentById_NotFound() {
    when(documentRepository.findById(1L)).thenReturn(Optional.empty());

    Optional<Document> result = documentService.getDocumentById(1L);

    assertTrue(result.isEmpty());
  }
}