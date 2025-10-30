package dev.coms4156.project.dtos;

import java.time.LocalDateTime;
import java.util.List;

import dev.coms4156.project.model.DocumentChunk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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

    List<DocumentRelationshipDTO> sourceRelationships = dc.getSourceRelationships() == null ? null
        : dc.getSourceRelationships().stream()
            .map(DocumentRelationshipDTO::fromDocumentRelationship).toList();

    List<DocumentRelationshipDTO> targetRelationships = dc.getTargetRelationships() == null ? null
        : dc.getTargetRelationships().stream()
            .map(DocumentRelationshipDTO::fromDocumentRelationship).toList();

    return new DocumentChunkDTO(
        dc.getId(),
        DocumentDTO.fromDocument(dc.getDocument()),
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
