package dev.coms4156.project.service;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for dividing document text into smaller, semantically meaningful chunks.
 */
@Service
public class DocumentChunkingService {

  private final DocumentChunkRepository documentChunkRepository;

  // Default chunking parameters
  private static final int DEFAULT_CHUNK_SIZE = 1000;
  private static final int DEFAULT_OVERLAP_SIZE = 200;
  private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[.!?]+\\s+");

  /**
   * Constructs a new {@code DocumentChunkingService} with the given repository.
   *
   * @param documentChunkRepository the {@link DocumentChunkRepository} instance for database ops.
   */
  public DocumentChunkingService(DocumentChunkRepository documentChunkRepository) {
    this.documentChunkRepository = documentChunkRepository;
  }

  /**
   * Chunk document text into smaller pieces.
   *
   * @param document    The document to chunk
   * @param chunkSize   Maximum size of each chunk (default: 1000)
   * @param overlapSize Number of characters to overlap between chunks (default:
   *                    200)
   * @return List of created document chunks
   */
  @Transactional
  public List<DocumentChunk> chunkDocument(Document document, Integer chunkSize,
                                           Integer overlapSize) {
    if (document == null || document.getExtractedText() == null
        || document.getExtractedText().trim().isEmpty()) {
      throw new IllegalArgumentException("Document or extracted text cannot be null or empty");
    }

    int actualChunkSize = chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE;
    int actualOverlapSize = overlapSize != null ? overlapSize : DEFAULT_OVERLAP_SIZE;

    String text = document.getExtractedText();
    List<DocumentChunk> chunks = new ArrayList<>();

    // Delete existing chunks for this document
    documentChunkRepository.deleteByDocument(document);

    int startIndex = 0;
    int chunkIndex = 0;

    while (startIndex < text.length()) {
      int endIndex = Math.min(startIndex + actualChunkSize, text.length());

      // Try to break at sentence boundaries
      if (endIndex < text.length()) {
        String chunkText = text.substring(startIndex, endIndex);
        int lastSentenceEnd = findLastSentenceEnd(chunkText);

        // Only break at sentence if it's not too short
        if (lastSentenceEnd > actualChunkSize * 0.7) {
          endIndex = startIndex + lastSentenceEnd;
        }
      }

      String chunkContent = text.substring(startIndex, endIndex).trim();

      if (!chunkContent.isEmpty()) {
        DocumentChunk chunk = DocumentChunk.builder()
            .document(document)
            .chunkIndex(chunkIndex)
            .textContent(chunkContent)
            .chunkSize(chunkContent.length())
            .startPosition(startIndex)
            .endPosition(endIndex)
            .embedding(null)
            .build();

        chunks.add(chunk); // Don't save yet - will save after embedding generation
        chunkIndex++;
      }

      // Move start index with overlap
      startIndex = Math.max(startIndex + actualChunkSize - actualOverlapSize, endIndex);

      // Prevent infinite loop
      if (startIndex >= endIndex) {
        startIndex = endIndex;
      }
    }

    return chunks;
  }

  /**
   * Chunk document with default parameters.
   *
   * @param document The document to chunk
   * @return List of created document chunks
   */
  @Transactional
  public List<DocumentChunk> chunkDocument(Document document) {
    return chunkDocument(document, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
  }

  /**
   * Find the last sentence end in the given text.
   *
   * @param text The text to search
   * @return Position of the last sentence end, or -1 if not found
   */
  private int findLastSentenceEnd(String text) {
    java.util.regex.Matcher matcher = SENTENCE_END_PATTERN.matcher(text);
    int lastEnd = -1;

    while (matcher.find()) {
      lastEnd = matcher.end();
    }

    return lastEnd;
  }

  /**
   * Get chunks for a specific document.
   *
   * @param document The document
   * @return List of document chunks
   */
  public List<DocumentChunk> getChunksForDocument(Document document) {
    return documentChunkRepository.findByDocumentOrderByChunkIndex(document);
  }

  /**
   * Get chunks for a specific document ID.
   *
   * @param documentId The document ID
   * @return List of document chunks
   */
  public List<DocumentChunk> getChunksForDocument(Long documentId) {
    return documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
  }

  /**
   * Delete all chunks for a document.
   *
   * @param document The document
   */
  @Transactional
  public void deleteChunksForDocument(Document document) {
    documentChunkRepository.deleteByDocument(document);
  }

  /**
   * Get chunk statistics for a document.
   *
   * @param document The document
   * @return Chunk statistics
   */
  public ChunkStatistics getChunkStatistics(Document document) {
    List<DocumentChunk> chunks = getChunksForDocument(document);

    if (chunks.isEmpty()) {
      return new ChunkStatistics(0, 0, 0, 0, 0);
    }

    int totalChunks = chunks.size();
    int totalCharacters = chunks.stream().mapToInt(DocumentChunk::getChunkSize).sum();
    int averageChunkSize = totalCharacters / totalChunks;
    int minChunkSize = chunks.stream().mapToInt(DocumentChunk::getChunkSize).min().orElse(0);
    int maxChunkSize = chunks.stream().mapToInt(DocumentChunk::getChunkSize).max().orElse(0);

    return new ChunkStatistics(totalChunks, totalCharacters, averageChunkSize, minChunkSize,
        maxChunkSize);
  }

  /**
   * Statistics about document chunks.
   */
  public static class ChunkStatistics {
    private final int totalChunks;
    private final int totalCharacters;
    private final int averageChunkSize;
    private final int minChunkSize;
    private final int maxChunkSize;

    /**
     * Initialize ChunkStatistics.
     */
    public ChunkStatistics(int totalChunks, int totalCharacters, int averageChunkSize,
                           int minChunkSize,
                           int maxChunkSize) {
      this.totalChunks = totalChunks;
      this.totalCharacters = totalCharacters;
      this.averageChunkSize = averageChunkSize;
      this.minChunkSize = minChunkSize;
      this.maxChunkSize = maxChunkSize;
    }

    // Getters

    /**
     * Get total number of chunks generated.
     */
    public int getTotalChunks() {
      return totalChunks;
    }

    /**
     * Get total number of characters across all chunks.
     */
    public int getTotalCharacters() {
      return totalCharacters;
    }

    /**
     * Get average chunk size in characters.
     */
    public int getAverageChunkSize() {
      return averageChunkSize;
    }

    /**
     * Get minimum chunk size encountered.
     */
    public int getMinChunkSize() {
      return minChunkSize;
    }

    /**
     * Get maximum chunk size encountered.
     */
    public int getMaxChunkSize() {
      return maxChunkSize;
    }
  }
}
