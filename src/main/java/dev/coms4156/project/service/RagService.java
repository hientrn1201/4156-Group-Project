package dev.coms4156.project.service;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Service for RAG (Retrieval-Augmented Generation) operations.
 * Uses existing document_chunks table for vector storage and retrieval.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RagService {

  private final DocumentChunkRepository documentChunkRepository;
  private final ChatClient chatClient;
  private final SimpleEmbeddingService embeddingService;

  /**
   * Query the LLM with RAG context retrieval from document_chunks table.
   *
   * @param question The user's question
   * @return The AI-generated response with context
   */
  public String queryWithRag(String question) {
    log.info("Processing RAG query: {}", question);

    // Search for relevant chunks using embedding service
    List<DocumentChunk> relevantChunks = embeddingService.findSimilarChunks(question, 5);

    // Build context from relevant chunks
    StringBuilder context = new StringBuilder();
    for (DocumentChunk chunk : relevantChunks) {
      context.append(chunk.getTextContent()).append("\n\n");
    }

    // Query with context
    return chatClient.prompt()
        .user("Context: " + context.toString() + "\n\nQuestion: " + question)
        .call()
        .content();
  }

  /**
   * Query the LLM without RAG (direct query).
   *
   * @param question The user's question
   * @return The AI-generated response
   */
  public String queryDirect(String question) {
    log.info("Processing direct query: {}", question);

    return chatClient.prompt()
        .user(question)
        .call()
        .content();
  }

  /**
   * Search for similar document chunks.
   *
   * @param query The search query
   * @param topK  Number of top results to return
   * @return List of similar document chunks
   */
  public List<DocumentChunk> searchSimilarDocuments(String query, int topK) {
    log.info("Searching for similar document chunks with query: {}", query);

    return embeddingService.findSimilarChunks(query, topK);
  }

  /**
   * Get statistics about the vector store.
   *
   * @return Map containing vector store statistics
   */
  public Map<String, Object> getVectorStoreStats() {
    Map<String, Object> stats = new java.util.HashMap<>();

    try {
      long totalChunks = documentChunkRepository.count();
      long chunksWithEmbeddings = documentChunkRepository.countByEmbeddingIsNotNull();

      stats.put("status", "active");
      stats.put("provider", "DocumentChunks");
      stats.put("model", "llama3.2");
      stats.put("dimensions", 3072);
      stats.put("totalChunks", totalChunks);
      stats.put("chunksWithEmbeddings", chunksWithEmbeddings);
      stats.put("embeddingCoverage",
          totalChunks > 0 ? (double) chunksWithEmbeddings / totalChunks : 0.0);
    } catch (Exception e) {
      stats.put("error", "Failed to get vector store statistics: " + e.getMessage());
    }

    return stats;
  }
}