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
public class DocumentChunkDto { // CHECKSTYLE.ON: AbbreviationAsWordInName

  private Long id;

  private DocumentDto document;

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

  private List<DocumentRelationshipDto> sourceRelationships;

  private List<DocumentRelationshipDto> targetRelationships;

  /**
   * Converts a DocumentChunk entity to a DocumentChunkDto.
   * Safely handles lazy-loaded collections and relationships to avoid
   * LazyInitializationException.
   *
   * @param dc the DocumentChunk entity to convert
   * @return the DocumentChunkDto, or null if the input is null
   */
  public static DocumentChunkDto fromDocumentChunk(DocumentChunk dc) {
    if (dc == null) {
      return null;
    }

    // Safely handle lazy-loaded collections - check if initialized before accessing
    List<DocumentRelationshipDto> sourceRelationships = null;
    if (dc.getSourceRelationships() != null
        && Hibernate.isInitialized(dc.getSourceRelationships())) {
      sourceRelationships = dc.getSourceRelationships().stream()
          .map(DocumentRelationshipDto::fromDocumentRelationship).toList();
    }

    List<DocumentRelationshipDto> targetRelationships = null;
    if (dc.getTargetRelationships() != null
        && Hibernate.isInitialized(dc.getTargetRelationships())) {
      targetRelationships = dc.getTargetRelationships().stream()
          .map(DocumentRelationshipDto::fromDocumentRelationship).toList();
    }

    // Safely handle lazy-loaded document - check if initialized before accessing
    DocumentDto documentDto = null;
    if (dc.getDocument() != null && Hibernate.isInitialized(dc.getDocument())) {
      documentDto = DocumentDto.fromDocument(dc.getDocument());
    }

    return new DocumentChunkDto(
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
