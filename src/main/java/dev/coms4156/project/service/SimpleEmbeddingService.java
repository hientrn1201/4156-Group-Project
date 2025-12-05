package dev.coms4156.project.service;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Embedding service using Spring AI with Ollama for local embedding generation
 * Uses llama3.2 model for 4096-dimensional embeddings.
 */
@Service
public class SimpleEmbeddingService {

  private static final Logger logger = LoggerFactory.getLogger(SimpleEmbeddingService.class);

  private final DocumentChunkRepository documentChunkRepository;
  private final EmbeddingModel embeddingModel;

  @Autowired
  public SimpleEmbeddingService(DocumentChunkRepository documentChunkRepository,
      EmbeddingModel embeddingModel) {
    this.documentChunkRepository = documentChunkRepository;
    this.embeddingModel = embeddingModel;
  }

  /**
   * Generates an embedding for a given {@link DocumentChunk} using the Ollama
   * embedding model.
   *
   * @param chunk the {@link DocumentChunk} containing text content to embed.
   * @return the same {@link DocumentChunk} with its embedding field populated.
   * @throws IllegalArgumentException if the chunk or its text content is null or
   *                                  empty.
   * @throws RuntimeException         if embedding generation or database
   *                                  insertion fails.
   */
  @Transactional
  public DocumentChunk generateEmbedding(DocumentChunk chunk) {
    if (chunk == null || chunk.getTextContent() == null
        || chunk.getTextContent().isBlank()) {
      throw new IllegalArgumentException("Chunk or text content cannot be null or empty");
    }

    try {
      String text = chunk.getTextContent();
      float[] embeddingArray = generateOllamaEmbeddingArray(text);

      // Convert to string format for native SQL
      String embeddingString = convertFloatArrayToVectorString(embeddingArray);

      // Use native SQL to insert with proper vector casting
      documentChunkRepository.insertChunkWithEmbedding(
          chunk.getChunkIndex(),
          chunk.getChunkSize(),
          chunk.getDocument().getId(),
          embeddingString,
          chunk.getEndPosition(),
          chunk.getStartPosition(),
          chunk.getTextContent());

      // Set embedding on chunk for return value
      chunk.setEmbedding(embeddingArray);
      return chunk;
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to generate embedding for chunk: " + e.getMessage(), e
      );
    }
  }

  /**
   * Generates embeddings for a list of {@link DocumentChunk} objects.
   *
   * @param chunks the list of {@link DocumentChunk} objects to process.
   * @return a list of successfully processed chunks with embeddings generated.
   */
  @Transactional
  public List<DocumentChunk> generateEmbeddings(List<DocumentChunk> chunks) {
    if (chunks == null || chunks.isEmpty()) {
      return new ArrayList<>();
    }

    List<DocumentChunk> processedChunks = new ArrayList<>();

    for (DocumentChunk chunk : chunks) {
      if (chunk.getTextContent() != null && !chunk.getTextContent().isBlank()) {
        try {
          float[] embeddingArray = generateOllamaEmbeddingArray(chunk.getTextContent());

          // Set embedding on chunk for return value
          chunk.setEmbedding(embeddingArray);
          chunk = documentChunkRepository.save(chunk);
          processedChunks.add(chunk);
        } catch (Exception e) {
          logger.error("Failed to generate embedding for chunk {}: {}", chunk.getId(),
              e.getMessage(), e);
          // Continue processing other chunks
        }
      }
    }

    return processedChunks;
  }

  /**
   * Generates a float array embedding for the given text using the Ollama
   * embedding model.
   *
   * @param text the text content to embed.
   * @return a float array representing the embedding vector.
   * @throws RuntimeException if the embedding generation fails or returns no
   *                          result.
   */
  private float[] generateOllamaEmbeddingArray(String text) {
    try {
      logger.debug("Generating embedding for text: {}...",
          text.substring(0, Math.min(100, text.length())));
      EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
      EmbeddingResponse response = embeddingModel.call(request);

      if (response.getResults() != null && !response.getResults().isEmpty()) {
        float[] embedding = response.getResults().get(0).getOutput();
        logger.debug("Successfully generated embedding with {} dimensions", embedding.length);
        return embedding;
      } else {
        throw new IllegalStateException("No embedding result returned from Ollama");
      }
    } catch (IllegalStateException e) {
      logger.error("Failed to generate Ollama embedding: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      logger.error("Failed to generate Ollama embedding: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to generate Ollama embedding: " + e.getMessage(), e);
    }
  }

  /**
   * Converts a float array into a PostgreSQL-compatible vector string format.
   *
   * @param vector the float array to convert.
   * @return the vector as a formatted string.
   * @throws IllegalArgumentException if the input array is null or empty.
   */
  private String convertFloatArrayToVectorString(float[] vector) {
    if (vector == null || vector.length == 0) {
      throw new IllegalArgumentException("Vector cannot be null or empty");
    }

    StringBuilder vectorString = new StringBuilder("[");
    for (int i = 0; i < vector.length; i++) {
      vectorString.append(vector[i]);
      if (i < vector.length - 1) {
        vectorString.append(',');
      }
    }
    vectorString.append(']');
    return vectorString.toString();
  }

  /**
   * Finds document chunks that are semantically similar to a given query text.
   *
   * @param queryText the input text used to find similar chunks.
   * @param limit     the maximum number of similar chunks to return.
   * @return a list of similar {@link DocumentChunk} results, or an empty list if
   *         none found.
   */
  public List<DocumentChunk> findSimilarChunks(String queryText, int limit) {
    if (queryText == null || queryText.isBlank()) {
      return new ArrayList<>();
    }

    try {
      // Generate embedding for the query text
      float[] queryEmbeddingArray = generateOllamaEmbeddingArray(queryText);
      String queryEmbedding = convertFloatArrayToVectorString(queryEmbeddingArray);

      logger.debug("Search query embedding dimensions: {}", queryEmbeddingArray.length);
      logger.debug("Search query embedding preview: {}...",
          queryEmbedding.substring(0, Math.min(50, queryEmbedding.length())));

      // Use PostgreSQL vector similarity search
      List<DocumentChunk> results = documentChunkRepository
          .findSimilarChunks(queryEmbedding, limit);
      logger.debug("Found {} similar chunks", results.size());
      return results;
    } catch (Exception e) {
      logger.error("Failed to find similar chunks: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Finds document chunks related to a given chunk based on embedding similarity.
   *
   * @param chunk the {@link DocumentChunk} to find related chunks for.
   * @param limit the maximum number of related chunks to return.
   * @return a list of related {@link DocumentChunk} results, or an empty list
   */
  public List<DocumentChunk> findRelatedChunks(DocumentChunk chunk, int limit) {
    if (chunk == null || chunk.getEmbedding() == null
        || chunk.getEmbedding().length == 0) {
      return new ArrayList<>();
    }

    if (limit == 0) {
      return new ArrayList<>();
    }

    try {
      float[] queryEmbeddingArray = chunk.getEmbedding();
      String queryEmbedding = convertFloatArrayToVectorString(queryEmbeddingArray);
      logger.debug("Chunk query embedding preview: {}...",
          queryEmbedding.substring(0, Math.min(50, queryEmbedding.length())));

      logger.debug("Chunk document ID: {}", chunk.getDocument().getId());

      logger.debug("Finding related chunks for chunk ID: {}", chunk.getId());

      // Use PostgreSQL vector similarity search
      List<DocumentChunk> results = documentChunkRepository.findRelatedChunks(
          chunk.getDocument().getId(), queryEmbedding, limit);
      logger.debug("Found {} related chunks", results.size());
      return results;
    } catch (Exception e) {
      logger.error("Failed to find related chunks: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Calculates the cosine similarity between two embedding vectors.
   *
   * @param embedding1 the first embedding vector.
   * @param embedding2 the second embedding vector.
   * @return a double value representing similarity in the range [-1, 1].
   */
  public double calculateSimilarity(float[] embedding1, float[] embedding2) {
    if (embedding1 == null || embedding2 == null || embedding1.length == 0
        || embedding2.length == 0) {
      return 0.0;
    }

    try {
      return calculateCosineSimilarity(embedding1, embedding2);
    } catch (Exception e) {
      logger.error("Failed to calculate similarity: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  /**
   * Computes cosine similarity between two equal-length float vectors.
   *
   * @param vector1 the first vector.
   * @param vector2 the second vector.
   * @return cosine similarity as a double.
   * @throws IllegalArgumentException if the two vectors have different lengths.
   */
  private double calculateCosineSimilarity(float[] vector1, float[] vector2) {
    if (vector1.length != vector2.length) {
      throw new IllegalArgumentException("Vectors must have the same dimension");
    }

    double dotProduct = 0.0;
    double norm1 = 0.0;
    double norm2 = 0.0;

    for (int i = 0; i < vector1.length; i++) {
      double v1 = vector1[i];
      double v2 = vector2[i];
      dotProduct += v1 * v2;
      norm1 += v1 * v1;
      norm2 += v2 * v2;
    }

    if (norm1 == 0.0 || norm2 == 0.0) {
      return 0.0;
    }

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
  }

  /**
   * Retrieves statistics about the stored embeddings.
   *
   * @return a map containing embedding statistics and model metadata.
   */
  public Map<String, Object> getEmbeddingStatistics() {
    Map<String, Object> stats = new HashMap<>();

    try {
      long totalChunks = documentChunkRepository.count();
      long chunksWithEmbeddings = documentChunkRepository.countByEmbeddingIsNotNull();

      stats.put("totalChunks", totalChunks);
      stats.put("chunksWithEmbeddings", chunksWithEmbeddings);
      stats.put("embeddingCoverage",
          totalChunks > 0 ? (double) chunksWithEmbeddings / totalChunks : 0.0);
      stats.put("model", "llama3.2");
      stats.put("dimensions", 4096);
      stats.put("provider", "Ollama");

    } catch (Exception e) {
      stats.put("error", "Failed to get embedding statistics: " + e.getMessage());
    }

    return stats;
  }

  /**
   * Generates embeddings for all chunks of a specific document that do not yet
   * have embeddings.
   *
   * @param documentId the ID of the document whose chunks should be embedded.
   * @return the number of chunks successfully processed.
   */
  @Transactional
  public int generateEmbeddingsForDocument(Long documentId) {
    List<DocumentChunk> chunks = documentChunkRepository
        .findByDocumentIdAndEmbeddingIsNull(documentId);
    if (chunks.isEmpty()) {
      return 0;
    }

    List<DocumentChunk> processedChunks = generateEmbeddings(chunks);
    return processedChunks.size();
  }

  /**
   * Tests the connectivity and functionality of the Ollama embedding model.
   *
   * @return a map containing connection status, message, and embedding metadata.
   */
  public Map<String, Object> testConnection() {
    Map<String, Object> result = new HashMap<>();

    try {
      String testText = "This is a test for Ollama embedding generation.";
      float[] embeddingArray = generateOllamaEmbeddingArray(testText);

      result.put("status", "success");
      result.put("message", "Ollama connection successful");
      result.put("testText", testText);
      result.put("embeddingGenerated", embeddingArray.length > 0);
      result.put("embeddingDimensions", embeddingArray.length);

    } catch (Exception e) {
      result.put("status", "error");
      result.put("message", "Ollama connection failed: " + e.getMessage());
    }

    return result;
  }
}
