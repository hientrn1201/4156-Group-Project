package dev.coms4156.project.repository;

import dev.coms4156.project.model.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Document entities.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

  List<Document> findByProcessingStatus(Document.ProcessingStatus status);

  List<Document> findByContentType(String contentType);

  @Query("SELECT d FROM Document d WHERE d.filename LIKE %:filename%")
  List<Document> findByFilenameContaining(@Param("filename") String filename);

  @Query("SELECT d FROM Document d WHERE d.summary IS NOT NULL")
  List<Document> findDocumentsWithSummaries();

  @Query("SELECT COUNT(d) FROM Document d WHERE d.processingStatus = :status")
  Long countByProcessingStatus(@Param("status") Document.ProcessingStatus status);
}