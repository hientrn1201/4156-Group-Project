package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.Arrays;
import java.util.List;
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

  // Test getVectorStoreStats success
  @Test
  void testGetVectorStoreStats_Success() {
    when(documentChunkRepository.count()).thenReturn(150L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(120L);

    java.util.Map<String, Object> stats = ragService.getVectorStoreStats();

    assertEquals("active", stats.get("status"));
    assertEquals("DocumentChunks", stats.get("provider"));
    assertEquals("llama3.2", stats.get("model"));
    assertEquals(3072, stats.get("dimensions"));
    assertEquals(150L, stats.get("totalChunks"));
    assertEquals(120L, stats.get("chunksWithEmbeddings"));
    assertEquals(0.8, stats.get("embeddingCoverage"));
  }

  // Test getVectorStoreStats with zero chunks
  @Test
  void testGetVectorStoreStats_ZeroChunks() {
    when(documentChunkRepository.count()).thenReturn(0L);
    when(documentChunkRepository.countByEmbeddingIsNotNull()).thenReturn(0L);

    java.util.Map<String, Object> stats = ragService.getVectorStoreStats();

    assertEquals(0L, stats.get("totalChunks"));
    assertEquals(0.0, stats.get("embeddingCoverage"));
  }

  // Test getVectorStoreStats with exception
  @Test
  void testGetVectorStoreStats_Exception() {
    when(documentChunkRepository.count()).thenThrow(new RuntimeException("Database error"));

    java.util.Map<String, Object> stats = ragService.getVectorStoreStats();

    org.junit.jupiter.api.Assertions.assertTrue(stats.containsKey("error"));
    org.junit.jupiter.api.Assertions.assertTrue(stats.get("error").toString().contains("Failed to get vector store statistics"));
  }

}