package dev.coms4156.project.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.coms4156.project.model.DocumentChunk;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.Hibernate;

/**
 * Data Transfer Object for DocumentChunk entities.
 * Used to transfer document chunk data between layers while avoiding lazy
 * loading issues.
 */
// CHECKSTYLE.OFF: AbbreviationAsWordInName
@Data
@AllArgsConstructor
@Builder
public class DocumentChunkDTO { // CHECKSTYLE.ON: AbbreviationAsWordInName

  private Long id;

  private DocumentDTO document;

  private Integer chunkIndex;

  private Long documentId;

  private String textContent;

  private Integer chunkSize;

  private Integer startPosition;

  private Integer endPosition;

  @JsonIgnore
  private float[] embedding;

  private String metadata;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private List<DocumentRelationshipDTO> sourceRelationships;

  private List<DocumentRelationshipDTO> targetRelationships;

  /**
   * Converts a DocumentChunk entity to a DocumentChunkDTO.
   * Safely handles lazy-loaded collections and relationships to avoid
   * LazyInitializationException.
   *
   * @param dc the DocumentChunk entity to convert
   * @return the DocumentChunkDTO, or null if the input is null
   */
  public static DocumentChunkDTO fromDocumentChunk(DocumentChunk dc) {
    if (dc == null) {
      return null;
    }

    // Safely handle lazy-loaded collections - check if initialized before accessing
    List<DocumentRelationshipDTO> sourceRelationships = null;
    if (dc.getSourceRelationships() != null
        && Hibernate.isInitialized(dc.getSourceRelationships())) {
      sourceRelationships = dc.getSourceRelationships().stream()
          .map(DocumentRelationshipDTO::fromDocumentRelationship).toList();
    }

    List<DocumentRelationshipDTO> targetRelationships = null;
    if (dc.getTargetRelationships() != null
        && Hibernate.isInitialized(dc.getTargetRelationships())) {
      targetRelationships = dc.getTargetRelationships().stream()
          .map(DocumentRelationshipDTO::fromDocumentRelationship).toList();
    }

    // Safely handle lazy-loaded document - check if initialized before accessing
    DocumentDTO documentDto = null;
    if (dc.getDocument() != null && Hibernate.isInitialized(dc.getDocument())) {
      documentDto = DocumentDTO.fromDocument(dc.getDocument());
    }

    return new DocumentChunkDTO(
        dc.getId(),
        documentDto,
        dc.getChunkIndex(),
        dc.getDocumentId(),
        dc.getTextContent(),
        dc.getChunkSize(),
        dc.getStartPosition(),
        dc.getEndPosition(),
        dc.getEmbedding(),
        dc.getMetadata(),
        dc.getCreatedAt(),
        dc.getUpdatedAt(),
        sourceRelationships,
        targetRelationships);
  }
}
