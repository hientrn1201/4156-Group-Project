package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

  @Mock
  private DocumentChunkRepository documentChunkRepository;

  @Mock
  private ChatClient chatClient;

  @Mock
  private SimpleEmbeddingService embeddingService;

  @Mock
  private ChatClient.ChatClientRequestSpec requestSpec;

  @Mock
  private ChatClient.CallResponseSpec responseSpec;

  private RagService ragService;

  @BeforeEach
  void setUp() {
    ragService = new RagService(documentChunkRepository, chatClient, embeddingService);
  }

  @Test
  void testQueryWithRag() {
    // Given
    String question = "What is machine learning?";

    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .textContent("Machine learning is a subset of artificial intelligence.")
        .build();
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .textContent("It involves algorithms that learn from data.")
        .build();

    List<DocumentChunk> relevantChunks = Arrays.asList(chunk1, chunk2);

    when(embeddingService.findSimilarChunks(question, 5)).thenReturn(relevantChunks);
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn("Machine learning is a powerful AI technique.");

    // When
    String result = ragService.queryWithRag(question);

    // Then
    assertEquals("Machine learning is a powerful AI technique.", result);
  }

  @Test
  void testSearchSimilarDocuments() {
    // Given
    String query = "machine learning algorithms";
    int topK = 3;

    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .textContent("Advanced machine learning algorithms for data analysis.")
        .build();

    List<DocumentChunk> expectedChunks = Arrays.asList(chunk);
    when(embeddingService.findSimilarChunks(query, topK)).thenReturn(expectedChunks);

    // When
    List<DocumentChunk> result = ragService.searchSimilarDocuments(query, topK);

    // Then
    assertEquals(1, result.size());
    assertEquals("Advanced machine learning algorithms for data analysis.",
        result.get(0).getTextContent());
  }

  @Test
  void testGetVectorStoreStats_Success() {
    // Given
    when(documentChunkRepository.count()).thenReturn(100L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(95L);

    // When
    Map<String, Object> result = ragService.getVectorStoreStats();

    // Then
    assertEquals("active", result.get("status"));
    assertEquals(100L, result.get("totalChunks"));
    assertEquals(95L, result.get("chunksWithEmbeddings"));
    assertEquals(0.95, (Double) result.get("embeddingCoverage"), 0.01);
  }

  @Test
  void testGetVectorStoreStats_ZeroChunks() {
    // Given
    when(documentChunkRepository.count()).thenReturn(0L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(0L);

    // When
    Map<String, Object> result = ragService.getVectorStoreStats();

    // Then
    assertEquals(0L, result.get("totalChunks"));
    assertEquals(0.0, (Double) result.get("embeddingCoverage"), 0.01);
  }

  @Test
  void testGetVectorStoreStats_Exception() {
    // Given
    when(documentChunkRepository.count()).thenThrow(new RuntimeException("Database error"));

    // When
    Map<String, Object> result = ragService.getVectorStoreStats();

    // Then - should catch exception and return error
    assertTrue(result.containsKey("error"));
    assertTrue(result.get("error").toString().contains("Failed to get vector store statistics"));
  }

  @Test
  void testQueryWithRag_EmptyChunks() {
    // Given
    String question = "What is AI?";
    when(embeddingService.findSimilarChunks(question, 5)).thenReturn(Arrays.asList());
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.content()).thenReturn("AI is artificial intelligence.");

    // When
    String result = ragService.queryWithRag(question);

    // Then - should still work with empty context
    assertEquals("AI is artificial intelligence.", result);
  }

}