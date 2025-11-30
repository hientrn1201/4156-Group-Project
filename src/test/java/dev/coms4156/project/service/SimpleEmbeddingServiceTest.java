package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.Arrays;
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

  @Test
  void testGenerateEmbeddings_NullList() {
    // When
    List<DocumentChunk> result = embeddingService.generateEmbeddings(null);

    // Then - should return empty list
    assertTrue(result.isEmpty());
  }

  @Test
  void testGenerateEmbeddings_EmptyList() {
    // When
    List<DocumentChunk> result = embeddingService.generateEmbeddings(Arrays.asList());

    // Then - should return empty list
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindSimilarChunks_NullQuery() {
    // When
    List<DocumentChunk> result = embeddingService.findSimilarChunks(null, 5);

    // Then - should return empty list
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindSimilarChunks_EmptyQuery() {
    // When
    List<DocumentChunk> result = embeddingService.findSimilarChunks("   ", 5);

    // Then - should return empty list
    assertTrue(result.isEmpty());
  }

  @Test
  void testCalculateSimilarity_NullEmbeddings() {
    // When
    double result = embeddingService.calculateSimilarity(null, new float[]{1.0f});

    // Then - should return 0.0
    assertEquals(0.0, result);
  }

  @Test
  void testCalculateSimilarity_EmptyEmbeddings() {
    // When
    double result = embeddingService.calculateSimilarity(new float[0], new float[]{1.0f});

    // Then - should return 0.0
    assertEquals(0.0, result);
  }

  @Test
  void testCalculateSimilarity_DifferentLengths() {
    // Given
    float[] embedding1 = {1.0f, 2.0f};
    float[] embedding2 = {1.0f, 2.0f, 3.0f};

    // When - should catch exception and return 0.0
    double result = embeddingService.calculateSimilarity(embedding1, embedding2);

    // Then - should return 0.0 (exception is caught internally)
    assertEquals(0.0, result);
  }

  @Test
  void testCalculateSimilarity_ZeroNorm() {
    // Given - zero vectors
    float[] embedding1 = {0.0f, 0.0f, 0.0f};
    float[] embedding2 = {0.0f, 0.0f, 0.0f};

    // When
    double result = embeddingService.calculateSimilarity(embedding1, embedding2);

    // Then - should return 0.0 for zero norm
    assertEquals(0.0, result);
  }

  @Test
  void testGenerateEmbeddingsForDocument_NoChunks() {
    // Given - no chunks found
    when(documentChunkRepository.findByDocumentIdAndEmbeddingIsNull(1L))
        .thenReturn(Arrays.asList());

    // When
    int result = embeddingService.generateEmbeddingsForDocument(1L);

    // Then - should return 0
    assertEquals(0, result);
  }

  @Test
  void testGenerateOllamaEmbeddingArray_EmptyResults() {
    // Given - embedding model returns empty results
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

    EmbeddingResponse mockResponse = new EmbeddingResponse(new ArrayList<>());
    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When & Then - should throw RuntimeException
    assertThrows(RuntimeException.class, () -> {
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
        .chunkIndex(0)
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .textContent("Test text")
        .build();

    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenThrow(new RuntimeException("Connection failed"));

    // When & Then - should throw RuntimeException
    assertThrows(RuntimeException.class, () -> {
      embeddingService.generateEmbedding(chunk);
    });
  }

  @Test
  void testTestConnection_Exception() {
    // Given - embedding model throws exception
    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenThrow(new RuntimeException("Connection failed"));

    // When
    Map<String, Object> result = embeddingService.testConnection();

    // Then - should return error status
    assertEquals("error", result.get("status"));
    assertTrue(result.get("message").toString().contains("Ollama connection failed"));
  }

  @Test
  void testCalculateCosineSimilarity_DifferentLengths_ThrowsException() {
    // Given - vectors with different lengths
    float[] embedding1 = {1.0f, 2.0f};
    float[] embedding2 = {1.0f, 2.0f, 3.0f};

    // When - calculateSimilarity catches the exception and returns 0.0
    // But we can test the private method indirectly by checking the behavior
    double result = embeddingService.calculateSimilarity(embedding1, embedding2);

    // Then - should return 0.0 (exception caught)
    assertEquals(0.0, result);
  }

  @Test
  void testGenerateEmbeddings_ExceptionDuringProcessing() {
    // Given - one chunk fails during embedding generation
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(50)
        .startPosition(0)
        .endPosition(50)
        .textContent("Valid chunk")
        .build();

    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .document(doc)
        .chunkIndex(1)
        .chunkSize(50)
        .startPosition(50)
        .endPosition(100)
        .textContent("Another valid chunk")
        .build();

    List<DocumentChunk> chunks = Arrays.asList(chunk1, chunk2);

    // Mock first call succeeds, second call fails
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenReturn(mockResponse)
        .thenThrow(new RuntimeException("Embedding failed"));

    // When - should continue processing and return only successful chunks
    List<DocumentChunk> result = embeddingService.generateEmbeddings(chunks);

    // Then - should return only the first chunk (second failed)
    assertEquals(1, result.size());
  }

  @Test
  void testGenerateEmbeddings_ChunkWithEmptyText() {
    // Given - chunk with empty text should be skipped
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(50)
        .startPosition(0)
        .endPosition(50)
        .textContent("   ") // Empty after trim
        .build();

    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .document(doc)
        .chunkIndex(1)
        .chunkSize(50)
        .startPosition(50)
        .endPosition(100)
        .textContent("Valid chunk")
        .build();

    List<DocumentChunk> chunks = Arrays.asList(chunk1, chunk2);

    // Mock embedding response
    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    List<DocumentChunk> result = embeddingService.generateEmbeddings(chunks);

    // Then - should only process chunk2 (chunk1 has empty text)
    assertEquals(1, result.size());
    assertEquals("Valid chunk", result.get(0).getTextContent());
  }

  @Test
  void testCalculateSimilarity_OneZeroNorm() {
    // Given - one vector has zero norm
    float[] embedding1 = {0.0f, 0.0f, 0.0f};
    float[] embedding2 = {1.0f, 2.0f, 3.0f};

    // When
    double result = embeddingService.calculateSimilarity(embedding1, embedding2);

    // Then - should return 0.0 for zero norm
    assertEquals(0.0, result);
  }

  @Test
  void testGetEmbeddingStatistics_ZeroTotalChunks() {
    // Given
    when(documentChunkRepository.count()).thenReturn(0L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(0L);

    // When
    Map<String, Object> result = embeddingService.getEmbeddingStatistics();

    // Then
    assertEquals(0L, result.get("totalChunks"));
    assertEquals(0.0, (Double) result.get("embeddingCoverage"), 0.01);
  }

  @Test
  void testGetEmbeddingStatistics_Exception() {
    // Given
    when(documentChunkRepository.count()).thenThrow(new RuntimeException("Database error"));

    // When
    Map<String, Object> result = embeddingService.getEmbeddingStatistics();

    // Then - should catch exception and return error
    assertTrue(result.containsKey("error"));
    assertTrue(result.get("error").toString().contains("Failed to get embedding statistics"));
  }

  @Test
  void testTestConnection_NullEmbeddingArray() {
    // Given - embedding model throws exception
    when(embeddingModel.call(any(EmbeddingRequest.class)))
        .thenThrow(new RuntimeException("Connection failed"));

    // When
    Map<String, Object> result = embeddingService.testConnection();

    // Then - should handle exception and return error status
    assertEquals("error", result.get("status"));
    // embeddingDimensions might be null or 0 when exception occurs
    Object dimensions = result.get("embeddingDimensions");
    assertTrue(dimensions == null || dimensions.equals(0));
  }

  @Test
  void testGenerateOllamaEmbeddingArray_ShortText() {
    // Given - text shorter than 100 characters (tests Math.min branch)
    Document doc = Document.builder().id(1L).build();
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(10)
        .startPosition(0)
        .endPosition(10)
        .textContent("Short")
        .build();

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    DocumentChunk result = embeddingService.generateEmbedding(chunk);

    // Then
    assertNotNull(result);
    assertNotNull(result.getEmbedding());
  }

  @Test
  void testGenerateOllamaEmbeddingArray_LongText() {
    // Given - text longer than 100 characters (tests Math.min branch)
    Document doc = Document.builder().id(1L).build();
    String longText = "a".repeat(200);
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .chunkSize(200)
        .startPosition(0)
        .endPosition(200)
        .textContent(longText)
        .build();

    float[] mockEmbedding = {0.1f, 0.2f, 0.3f};
    Embedding embedding = new Embedding(mockEmbedding, 0);
    EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

    when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

    // When
    DocumentChunk result = embeddingService.generateEmbedding(chunk);

    // Then
    assertNotNull(result);
    assertNotNull(result.getEmbedding());
  }

  @Test
  void testGetEmbeddingStatistics_WithChunks() {
    // Given
    when(documentChunkRepository.count()).thenReturn(100L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(80L);

    // When
    Map<String, Object> result = embeddingService.getEmbeddingStatistics();

    // Then
    assertEquals(100L, result.get("totalChunks"));
    assertEquals(80L, result.get("chunksWithEmbeddings"));
    assertEquals(0.8, (Double) result.get("embeddingCoverage"), 0.01);
    assertEquals("llama3.2", result.get("model"));
    assertEquals(4096, result.get("dimensions"));
  }
}