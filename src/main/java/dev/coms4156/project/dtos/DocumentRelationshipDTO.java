package dev.coms4156.project.dtos;

import dev.coms4156.project.model.DocumentRelationship;
import dev.coms4156.project.model.DocumentRelationship.RelationshipType;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentRelationshipDTO {
  private Long id;

  private Long sourceChunkId;

  private Long targetChunkId;

  private RelationshipType relationshipType;

  private Double similarityScore;

  private Double confidenceScore;

  private String metadata;

  private LocalDateTime createdAt;

  public static DocumentRelationshipDTO fromDocumentRelationship(DocumentRelationship dr) {
    if (dr == null) {
      return null;
    }

    return new DocumentRelationshipDTO(
        dr.getId(),
        dr.getSourceChunk().getId(),
        dr.getTargetChunk().getId(),
        dr.getRelationshipType(),
        dr.getSimilarityScore(),
        dr.getConfidenceScore(),
        dr.getMetadata(),
        dr.getCreatedAt());
  }
}
