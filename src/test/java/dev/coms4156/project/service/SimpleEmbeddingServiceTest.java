package dev.coms4156.project.service;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        DocumentChunk chunk = DocumentChunk.builder()
                .id(1L)
                .textContent("This is a test document about machine learning and artificial intelligence.")
                .build();

        // Mock the embedding model response
        float[] mockEmbedding = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f };
        Embedding embedding = new Embedding(mockEmbedding, 0);
        EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

        when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);
        when(documentChunkRepository.save(any(DocumentChunk.class))).thenReturn(chunk);

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
        DocumentChunk chunk1 = DocumentChunk.builder()
                .id(1L)
                .textContent("First chunk about machine learning.")
                .build();

        DocumentChunk chunk2 = DocumentChunk.builder()
                .id(2L)
                .textContent("Second chunk about artificial intelligence.")
                .build();

        List<DocumentChunk> chunks = Arrays.asList(chunk1, chunk2);

        // Mock the embedding model response
        float[] mockEmbedding = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f };
        Embedding embedding = new Embedding(mockEmbedding, 0);
        EmbeddingResponse mockResponse = new EmbeddingResponse(Arrays.asList(embedding));

        when(embeddingModel.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);
        when(documentChunkRepository.saveAll(any(List.class))).thenReturn(chunks);

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
        float[] embedding1 = { 1.0f, 0.0f, 0.0f };
        float[] embedding2 = { 1.0f, 0.0f, 0.0f };

        // When
        double similarity = embeddingService.calculateSimilarity(embedding1, embedding2);

        // Then
        assertEquals(1.0, similarity, 0.001); // Identical vectors should have similarity 1.0
    }

    @Test
    void testTestConnection() {
        // Given
        float[] mockEmbedding = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f };
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
}
