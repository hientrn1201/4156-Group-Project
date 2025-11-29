package dev.coms4156.project.dtos;

import dev.coms4156.project.model.DocumentRelationship;
import dev.coms4156.project.model.DocumentRelationship.RelationshipType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for DocumentRelationship entities.
 * Used to transfer document relationship data between layers.
 */
// CHECKSTYLE.OFF: AbbreviationAsWordInName
@Data
@AllArgsConstructor
@Builder
public class DocumentRelationshipDTO { // CHECKSTYLE.ON: AbbreviationAsWordInName
  private Long id;

  private Long sourceChunkId;

  private Long targetChunkId;

  private RelationshipType relationshipType;

  private Double similarityScore;

  private Double confidenceScore;

  private String metadata;

  private LocalDateTime createdAt;

  /**
   * Converts a DocumentRelationship entity to a DocumentRelationshipDTO.
   *
   * @param dr the DocumentRelationship entity to convert
   * @return the DocumentRelationshipDTO, or null if the input is null
   */
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
