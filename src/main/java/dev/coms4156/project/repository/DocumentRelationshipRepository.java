package dev.coms4156.project.repository;

import dev.coms4156.project.model.DocumentRelationship;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DocumentRelationship entities.
 */
@Repository
public interface DocumentRelationshipRepository extends JpaRepository<DocumentRelationship, Long> {

  List<DocumentRelationship> findBySourceChunkId(Long sourceChunkId);

  List<DocumentRelationship> findByTargetChunkId(Long targetChunkId);

  List<DocumentRelationship> findByRelationshipType(
      DocumentRelationship.RelationshipType relationshipType);

  long countByRelationshipType(DocumentRelationship.RelationshipType relationshipType);

  @Query("SELECT dr FROM DocumentRelationship dr WHERE "
      + "dr.sourceChunk.document.id = :documentId OR dr.targetChunk.document.id = :documentId")
  List<DocumentRelationship> findByDocumentId(@Param("documentId") Long documentId);

  @Query("SELECT dr FROM DocumentRelationship dr WHERE dr.similarityScore >= :threshold "
      + "ORDER BY dr.similarityScore DESC")
  List<DocumentRelationship> findBySimilarityScoreGreaterThanEqual(
      @Param("threshold") Double threshold);

  /**
   * Deletes all relationships associated with chunks of a given document using
   * native SQL.
   * This avoids loading entities with embeddings which can cause converter
   * issues.
   *
   * @param documentId the document ID
   * @return the number of relationships deleted
   */
  @Modifying
  @Query(value = "DELETE FROM document_relationships "
      + "WHERE source_chunk_id IN (SELECT id FROM document_chunks WHERE document_id = :documentId) "
      +
      "OR target_chunk_id IN (SELECT id FROM document_chunks WHERE document_id = :documentId)",
      nativeQuery = true)
  int deleteByDocumentIdNative(@Param("documentId") Long documentId);
}
