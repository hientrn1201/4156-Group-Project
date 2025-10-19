package dev.coms4156.project.service;

import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Embedding service using PostgresML for local embedding generation
 * Uses distilbert-base-uncased model for 768-dimensional embeddings
 */
@Service
public class SimpleEmbeddingService {

    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public SimpleEmbeddingService(DocumentChunkRepository documentChunkRepository,
            EmbeddingModel embeddingModel) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Generate embedding using PostgresML
     */
    @Transactional
    public DocumentChunk generateEmbedding(DocumentChunk chunk) {
        if (chunk == null || chunk.getTextContent() == null || chunk.getTextContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Chunk or text content cannot be null or empty");
        }

        try {
            String text = chunk.getTextContent();
            float[] embeddingArray = generatePostgresMLEmbeddingArray(text);

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
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate embedding for chunk: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for multiple chunks
     */
    @Transactional
    public List<DocumentChunk> generateEmbeddings(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return new ArrayList<>();
        }

        List<DocumentChunk> processedChunks = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk.getTextContent() != null && !chunk.getTextContent().trim().isEmpty()) {
                try {
                    float[] embeddingArray = generatePostgresMLEmbeddingArray(chunk.getTextContent());
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
                    processedChunks.add(chunk);
                } catch (Exception e) {
                    System.err
                            .println("Failed to generate embedding for chunk " + chunk.getId() + ": " + e.getMessage());
                    // Continue processing other chunks
                }
            }
        }

        return processedChunks;
    }

    /**
     * Generate embedding using PostgresML EmbeddingModel
     */
    private float[] generatePostgresMLEmbeddingArray(String text) {
        try {
            System.out.println(
                    "Generating embedding for text: " + text.substring(0, Math.min(100, text.length())) + "...");
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse response = embeddingModel.call(request);

            if (response.getResults() != null && !response.getResults().isEmpty()) {
                float[] embedding = response.getResults().get(0).getOutput();
                System.out.println("Successfully generated embedding with " + embedding.length + " dimensions");
                return embedding;
            } else {
                throw new RuntimeException("No embedding result returned from PostgresML");
            }
        } catch (Exception e) {
            System.err.println("Failed to generate PostgresML embedding: " + e.getMessage());
            throw new RuntimeException("Failed to generate PostgresML embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Convert float array directly to PostgreSQL vector string format
     */
    private String convertFloatArrayToVectorString(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }

        StringBuilder vectorString = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            vectorString.append(vector[i]);
            if (i < vector.length - 1) {
                vectorString.append(",");
            }
        }
        vectorString.append("]");
        return vectorString.toString();
    }

    /**
     * Find similar chunks based on embedding similarity
     */
    public List<DocumentChunk> findSimilarChunks(String queryText, int limit) {
        if (queryText == null || queryText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Generate embedding for the query text
            float[] queryEmbeddingArray = generatePostgresMLEmbeddingArray(queryText);
            String queryEmbedding = convertFloatArrayToVectorString(queryEmbeddingArray);

            System.out.println("Search query embedding dimensions: " + queryEmbeddingArray.length);
            System.out.println("Search query embedding preview: "
                    + queryEmbedding.substring(0, Math.min(50, queryEmbedding.length())) + "...");

            // Use PostgreSQL vector similarity search
            List<DocumentChunk> results = documentChunkRepository.findSimilarChunks(queryEmbedding, limit);
            System.out.println("Found " + results.size() + " similar chunks");
            return results;
        } catch (Exception e) {
            System.err.println("Failed to find similar chunks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find similar chunks for a given chunk
     */
    public List<DocumentChunk> findSimilarChunks(DocumentChunk chunk, int limit) {
        if (chunk == null || chunk.getEmbedding() == null) {
            return new ArrayList<>();
        }

        try {
            String embeddingString = convertFloatArrayToVectorString(chunk.getEmbedding());
            return documentChunkRepository.findSimilarChunks(embeddingString, limit);
        } catch (Exception e) {
            System.err.println("Failed to find similar chunks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double calculateSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null || embedding1.length == 0 || embedding2.length == 0) {
            return 0.0;
        }

        try {
            return calculateCosineSimilarity(embedding1, embedding2);
        } catch (Exception e) {
            System.err.println("Failed to calculate similarity: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate cosine similarity between two vectors
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
     * Get embedding statistics
     */
    public Map<String, Object> getEmbeddingStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalChunks = documentChunkRepository.count();
            long chunksWithEmbeddings = documentChunkRepository.countByEmbeddingIsNotNull();

            stats.put("totalChunks", totalChunks);
            stats.put("chunksWithEmbeddings", chunksWithEmbeddings);
            stats.put("embeddingCoverage", totalChunks > 0 ? (double) chunksWithEmbeddings / totalChunks : 0.0);
            stats.put("model", "distilbert-base-uncased");
            stats.put("dimensions", 768);
            stats.put("provider", "PostgresML");

        } catch (Exception e) {
            stats.put("error", "Failed to get embedding statistics: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Generate embeddings for all chunks of a specific document
     */
    @Transactional
    public int generateEmbeddingsForDocument(Long documentId) {
        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdAndEmbeddingIsNull(documentId);
        if (chunks.isEmpty()) {
            return 0;
        }

        List<DocumentChunk> processedChunks = generateEmbeddings(chunks);
        return processedChunks.size();
    }

    /**
     * Test PostgresML connection
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();

        try {
            String testText = "This is a test for PostgresML embedding generation.";
            float[] embeddingArray = generatePostgresMLEmbeddingArray(testText);

            result.put("status", "success");
            result.put("message", "PostgresML connection successful");
            result.put("testText", testText);
            result.put("embeddingGenerated", embeddingArray != null && embeddingArray.length > 0);
            result.put("embeddingDimensions", embeddingArray != null ? embeddingArray.length : 0);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "PostgresML connection failed: " + e.getMessage());
        }

        return result;
    }
}