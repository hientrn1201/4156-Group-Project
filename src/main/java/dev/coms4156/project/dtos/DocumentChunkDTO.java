package dev.coms4156.project.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.coms4156.project.model.DocumentChunk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.Hibernate;

@Data
@AllArgsConstructor
@Builder
public class DocumentChunkDTO {

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

  public static DocumentChunkDTO fromDocumentChunk(DocumentChunk dc) {
    if (dc == null) {
      return null;
    }

    // Safely handle lazy-loaded collections - check if initialized before accessing
    List<DocumentRelationshipDTO> sourceRelationships = null;
    if (dc.getSourceRelationships() != null &&
        Hibernate.isInitialized(dc.getSourceRelationships())) {
      sourceRelationships = dc.getSourceRelationships().stream()
          .map(DocumentRelationshipDTO::fromDocumentRelationship).toList();
    }

    List<DocumentRelationshipDTO> targetRelationships = null;
    if (dc.getTargetRelationships() != null &&
        Hibernate.isInitialized(dc.getTargetRelationships())) {
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
