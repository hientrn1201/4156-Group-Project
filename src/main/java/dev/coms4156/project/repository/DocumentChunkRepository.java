package dev.coms4156.project.repository;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DocumentChunk entities.
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

  List<DocumentChunk> findByDocumentId(Long documentId);

  @Query("SELECT dc FROM DocumentChunk dc WHERE dc.document.id = :documentId "
      + "ORDER BY dc.chunkIndex")
  List<DocumentChunk> findByDocumentIdOrderByChunkIndex(@Param("documentId") Long documentId);

  @Query("SELECT dc FROM DocumentChunk dc WHERE dc.document = :document "
      + "ORDER BY dc.chunkIndex")
  List<DocumentChunk> findByDocumentOrderByChunkIndex(@Param("document") Document document);

  @Query(value = "SELECT * FROM document_chunks WHERE embedding IS NOT NULL", nativeQuery = true)
  List<DocumentChunk> findChunksWithEmbeddings();

  @Query(value = "SELECT * FROM document_chunks WHERE document_id = :documentId "
      + "AND embedding IS NOT NULL", nativeQuery = true)
  List<DocumentChunk> findChunksWithEmbeddingsByDocumentId(@Param("documentId") Long documentId);

  @Query(value = "SELECT * FROM document_chunks WHERE document_id = :documentId "
      + "AND embedding IS NULL", nativeQuery = true)
  List<DocumentChunk> findByDocumentIdAndEmbeddingIsNull(@Param("documentId") Long documentId);

  @Query(value = "SELECT * FROM document_chunks WHERE embedding IS NOT NULL", nativeQuery = true)
  List<DocumentChunk> findByEmbeddingIsNotNull();

  @Query(value = "SELECT COUNT(*) FROM document_chunks WHERE embedding IS NOT NULL",
      nativeQuery = true)
  Long countByEmbeddingIsNotNull();

  @Query(value = "SELECT * FROM document_chunks WHERE embedding IS NOT NULL "
      + "ORDER BY embedding <-> CAST(:queryEmbedding AS vector) LIMIT :limit", nativeQuery = true)
  List<DocumentChunk> findSimilarChunks(@Param("queryEmbedding") String queryEmbedding,
                                        @Param("limit") int limit);

  @Modifying
  @Query("DELETE FROM DocumentChunk dc WHERE dc.document = :document")
  void deleteByDocument(@Param("document") Document document);

  @Modifying
  @Query(value = "INSERT INTO document_chunks (chunk_index, chunk_size, document_id, "
      + "embedding, end_position, start_position, text_content) VALUES "
      + "(:chunkIndex, :chunkSize, :documentId, CAST(:embedding AS vector), "
      + ":endPosition, :startPosition, :textContent)", nativeQuery = true)
  void insertChunkWithEmbedding(@Param("chunkIndex") Integer chunkIndex,
                                @Param("chunkSize") Integer chunkSize,
                                @Param("documentId") Long documentId,
                                @Param("embedding") String embedding,
                                @Param("endPosition") Integer endPosition,
                                @Param("startPosition") Integer startPosition,
                                @Param("textContent") String textContent);
}
