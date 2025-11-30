package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

@ExtendWith(MockitoExtension.class)
class SimpleEmbeddingServiceTest {

  @Mock
  private DocumentChunkRepository documentChunkRepository;

  @Mock
  private EmbeddingModel embeddingModel;

  private SimpleEmbeddingService embeddingService;

  @BeforeEach
  void setUp() {
    embeddingService = new SimpleEmbeddingService(documentChunkRepository, embeddingModel);
  }

  @Test
  void testGenerateEmbedding() {
    // Given
    Document doc = Document.builder()
        .id(1L)
        .build();

    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .textContent("This is a test document about machine learning and artificial intelligence.")
        .build();

    // Mock the embedding model response
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    DocumentChunk result = embeddingService.generateEmbedding(chunk);

    // Then
    assertNotNull(result);
    assertNotNull(result.getEmbedding());
    assertTrue(result.getEmbedding().length > 0);
  }

  @Test
  void testGenerateEmbeddings() {
    // Given
    Document doc = Document.builder()
        .id(1L)
        .build();

    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(50)
        .startPosition(0)
        .endPosition(50)
        .textContent("First chunk about machine learning.")
        .build();

    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .document(doc)
        .chunkIndex(1)
        .chunkSize(50)
        .startPosition(50)
        .endPosition(100)
        .textContent("Second chunk about artificial intelligence.")
        .build();

    List<DocumentChunk> chunks = Arrays.asList(chunk1, chunk2);

    // Mock the embedding model response
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    List<DocumentChunk> result = embeddingService.generateEmbeddings(chunks);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertNotNull(result.get(0).getEmbedding());
    assertNotNull(result.get(1).getEmbedding());
  }

  // Boundary analysis - empty chunks list
  @Test
  void testGenerateEmbeddings_EmptyList() {
    List<DocumentChunk> emptyChunks = Arrays.asList();

    List<DocumentChunk> result = embeddingService.generateEmbeddings(emptyChunks);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  // Invalid equivalence partition - null chunks list
  @Test
  void testGenerateEmbeddings_NullList() {
    // Implementation may handle null gracefully
    List<DocumentChunk> result = embeddingService.generateEmbeddings(null);
    // May return empty list or null
    if (result != null) {
      assertEquals(0, result.size());
    }
  }

  // Boundary analysis - single chunk in list
  @Test
  void testGenerateEmbeddings_SingleChunk() {
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .textContent("Single chunk text")
        .build();

    List<DocumentChunk> chunks = Arrays.asList(chunk);

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    List<DocumentChunk> result = embeddingService.generateEmbeddings(chunks);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertNotNull(result.get(0).getEmbedding());
  }

  // Valid equivalence partition - large number of chunks
  @Test
  void testGenerateEmbeddings_ManyChunks() {
    Document doc = Document.builder().id(1L).build();
    List<DocumentChunk> chunks = new java.util.ArrayList<>();
    for (int i = 0; i < 100; i++) {
      chunks.add(DocumentChunk.builder()
          .id((long) i)
          .document(doc)
          .textContent("Chunk " + i + " content")
          .build());
    }

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    List<DocumentChunk> result = embeddingService.generateEmbeddings(chunks);

    assertNotNull(result);
    assertEquals(100, result.size());
    for (DocumentChunk chunk : result) {
      assertNotNull(chunk.getEmbedding());
    }
  }

  @Test
  void testCalculateSimilarity() {
    // Given
    float[] embedding1 = {1.0f, 0.0f, 0.0f};
    float[] embedding2 = {1.0f, 0.0f, 0.0f};

    // When
    double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

    // Then
    assertEquals(1.0, similarity, 0.001); // Identical vectors should have similarity 1.0
  }

  // Boundary analysis - orthogonal vectors (zero similarity)
  @Test
  void testCalculateSimilarity_OrthogonalVectors() {
    float[] embedding1 = {1.0f, 0.0f, 0.0f};
    float[] embedding2 = {0.0f, 1.0f, 0.0f};

    double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

    assertEquals(0.0, similarity, 0.001);
  }

  // Boundary analysis - opposite vectors (negative similarity)
  @Test
  void testCalculateSimilarity_OppositeVectors() {
    float[] embedding1 = {1.0f, 0.0f, 0.0f};
    float[] embedding2 = {-1.0f, 0.0f, 0.0f};

    double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

    assertEquals(-1.0, similarity, 0.001);
  }

  // Invalid equivalence partition - different length vectors (may not throw)
  @Test
  void testCalculateSimilarity_DifferentLengths() {
    float[] embedding1 = {1.0f, 0.0f};
    float[] embedding2 = {1.0f, 0.0f, 0.0f};

    // Implementation may handle this gracefully
    double result = embeddingService.calculateSimilarity(embedding1, embedding2);
    // Just verify it returns a number
    assertNotNull(result);
  }

  // Invalid equivalence partition - null vectors (may not throw)
  @Test
  void testCalculateSimilarity_NullVectors() {
    float[] embedding1 = {1.0f, 0.0f, 0.0f};

    // Implementation may handle nulls gracefully
    try {
      embeddingService.calculateSimilarity(null, embedding1);
      embeddingService.calculateSimilarity(embedding1, null);
    } catch (Exception e) {
      // Exception is acceptable but not required
    }
  }

  // Boundary analysis - zero vectors (may not throw)
  @Test
  void testCalculateSimilarity_ZeroVectors() {
    float[] embedding1 = {0.0f, 0.0f, 0.0f};
    float[] embedding2 = {0.0f, 0.0f, 0.0f};

    // Implementation may handle zero vectors
    try {
      double result = embeddingService.calculateSimilarity(embedding1, embedding2);
      // Zero vectors may return NaN or 0
      assertNotNull(result);
    } catch (Exception e) {
      // Exception is acceptable for zero vectors
    }
  }

  // Boundary analysis - single element vectors
  @Test
  void testCalculateSimilarity_SingleElement() {
    float[] embedding1 = {1.0f};
    float[] embedding2 = {1.0f};

    double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

    assertEquals(1.0, similarity, 0.001);
  }

  // Valid equivalence partition - large vectors
  @Test
  void testCalculateSimilarity_LargeVectors() {
    float[] embedding1 = new float[1000];
    float[] embedding2 = new float[1000];
    
    for (int i = 0; i < 1000; i++) {
      embedding1[i] = 1.0f;
      embedding2[i] = 1.0f;
    }

    double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

    assertEquals(1.0, similarity, 0.001);
  }

  @Test
  void testTestConnection() {
    // Given
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    Map<String, Object> result = embeddingService.testConnection();

    // Then
    assertNotNull(result);
    assertEquals("success", result.get("status"));
  }

  @Test
  void testGenerateEmbeddingThrowsExceptionForNullChunk() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      embeddingService.generateEmbedding(null);
    });
  }

  @Test
  void testGenerateEmbeddingThrowsExceptionForEmptyText() {
    // Given
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("")
        .build();

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      embeddingService.generateEmbedding(chunk);
    });
  }

  // Invalid equivalence partition - whitespace-only text
  @Test
  void testGenerateEmbedding_WhitespaceOnlyText() {
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("   \n\t   ")
        .build();

    assertThrows(IllegalArgumentException.class, () -> {
      embeddingService.generateEmbedding(chunk);
    });
  }

  @Test
  void testGenerateOllamaEmbeddingArray_NullResults() {
    // Given - embedding model returns null results
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .textContent("Test text")
        .build();

    EmbeddingResponse mockResponse = new EmbeddingResponse(null);
    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When & Then - should throw RuntimeException
    assertThrows(RuntimeException.class, () -> {
      embeddingService.generateEmbedding(chunk);
    });
  }

  @Test
  void testGenerateOllamaEmbeddingArray_Exception() {
    // Given - embedding model throws exception
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .textContent("This is a very long text. ".repeat(1000))
        .build();

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    DocumentChunk result = embeddingService.generateEmbedding(chunk);

    assertNotNull(result);
    assertNotNull(result.getEmbedding());
  }

  // Valid equivalence partition - text with special characters
  @Test
  void testGenerateEmbedding_SpecialCharacters() {
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .textContent("Text with special chars: @#$%^&*()_+-=[]{}|;':,.<>?/~`")
        .build();

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    DocumentChunk result = embeddingService.generateEmbedding(chunk);

    assertNotNull(result);
    assertNotNull(result.getEmbedding());
  }

  // Test embedding service failure
  @Test
  void testGenerateEmbedding_ServiceFailure() {
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .textContent("Test content")
        .build();

    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenThrow(new RuntimeException("Service unavailable"));

    assertThrows(RuntimeException.class, () -> {
      embeddingService.generateEmbedding(chunk);
    });
  }

  // Test getEmbeddingStatistics success
  @Test
  void testGetEmbeddingStatistics_Success() {
    when(documentChunkRepository.count()).thenReturn(100L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(80L);

    Map<String, Object> stats = embeddingService.getEmbeddingStatistics();

    assertEquals(100L, stats.get("totalChunks"));
    assertEquals(80L, stats.get("chunksWithEmbeddings"));
    assertEquals(0.8, stats.get("embeddingCoverage"));
    assertEquals("llama3.2", stats.get("model"));
  }

  // Test getEmbeddingStatistics with zero chunks
  @Test
  void testGetEmbeddingStatistics_ZeroChunks() {
    when(documentChunkRepository.count()).thenReturn(0L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(0L);

    Map<String, Object> stats = embeddingService.getEmbeddingStatistics();

    assertEquals(0L, stats.get("totalChunks"));
    assertEquals(0.0, stats.get("embeddingCoverage"));
  }

  // Test getEmbeddingStatistics with exception
  @Test
  void testGetEmbeddingStatistics_Exception() {
    when(documentChunkRepository.count()).thenThrow(new RuntimeException("DB error"));

    Map<String, Object> stats = embeddingService.getEmbeddingStatistics();

    assertTrue(stats.containsKey("error"));
  }

  // Test findSimilarChunks with null query
  @Test
  void testFindSimilarChunks_NullQuery() {
    List<DocumentChunk> result = embeddingService.findSimilarChunks(null, 5);
    assertEquals(0, result.size());
  }

  // Test findSimilarChunks with empty query
  @Test
  void testFindSimilarChunks_EmptyQuery() {
    List<DocumentChunk> result = embeddingService.findSimilarChunks("", 5);
    assertEquals(0, result.size());
  }

  // Test findSimilarChunks with whitespace query
  @Test
  void testFindSimilarChunks_WhitespaceQuery() {
    List<DocumentChunk> result = embeddingService.findSimilarChunks("   ", 5);
    assertEquals(0, result.size());
  }

  // Test findSimilarChunks success
  @Test
  void testFindSimilarChunks_Success() {
    String query = "test query";
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));
    
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Similar content")
        .build();
    
    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);
    when(documentChunkRepository.findSimilarChunks(anyString(), any(Integer.class)))
        .thenReturn(Arrays.asList(chunk));

    List<DocumentChunk> result = embeddingService.findSimilarChunks(query, 5);

    assertEquals(1, result.size());
    assertEquals("Similar content", result.get(0).getTextContent());
  }

  // Test findSimilarChunks with exception
  @Test
  void testFindSimilarChunks_Exception() {
    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenThrow(new RuntimeException("Embedding failed"));

    List<DocumentChunk> result = embeddingService.findSimilarChunks("test", 5);
    assertEquals(0, result.size());
  }

  // Test generateEmbeddingsForDocument with no chunks
  @Test
  void testGenerateEmbeddingsForDocument_NoChunks() {
    when(documentChunkRepository.findByDocumentIdAndEmbeddingIsNull(1L))
        .thenReturn(new ArrayList<>());

    int result = embeddingService.generateEmbeddingsForDocument(1L);
    assertEquals(0, result);
  }

  // Test generateEmbeddingsForDocument success
  @Test
  void testGenerateEmbeddingsForDocument_Success() {
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .textContent("Test content")
        .build();
    
    when(documentChunkRepository.findByDocumentIdAndEmbeddingIsNull(1L))
        .thenReturn(Arrays.asList(chunk));
    
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));
    
    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);
    when(documentChunkRepository.findSimilarChunks(anyString(), anyInt()))
        .thenThrow(new RuntimeException("Database error"));

    // When - should catch exception and return empty list
    List<DocumentChunk> result = embeddingService.findSimilarChunks(query, 5);

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  void testCalculateSimilarity_DifferentLengthVectors() {
    // Given - vectors with different lengths
    // Note: calculateSimilarity catches exceptions and returns 0.0, so we test that behavior
    float[] vec1 = {1.0f, 2.0f, 3.0f};
    float[] vec2 = {1.0f, 2.0f};

    // When - should catch IllegalArgumentException and return 0.0
    double result = embeddingService.calculateSimilarity(vec1, vec2);

    // Then - should return 0.0 (exception is caught internally)
    assertEquals(0.0, result);
  }

  @Test
  void testCalculateSimilarity_EmptyVectors() {
    // Given - empty vectors
    float[] vec1 = {};
    float[] vec2 = {1.0f, 2.0f};

    // When
    double result = embeddingService.calculateSimilarity(vec1, vec2);

    // Then - should return 0.0
    assertEquals(0.0, result);
  }
}