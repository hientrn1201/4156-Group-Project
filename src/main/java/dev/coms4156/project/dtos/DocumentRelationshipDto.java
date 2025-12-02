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
public class DocumentRelationshipDto { // CHECKSTYLE.ON: AbbreviationAsWordInName
  private Long id;

  private Long sourceChunkId;

  private Long targetChunkId;

  private RelationshipType relationshipType;

  private Double similarityScore;

  private Double confidenceScore;

  private String metadata;

  private LocalDateTime createdAt;

  /**
   * Converts a DocumentRelationship entity to a DocumentRelationshipDto.
   *
   * @param dr the DocumentRelationship entity to convert
   * @return the DocumentRelationshipDto, or null if the input is null
   */
  public static DocumentRelationshipDto fromDocumentRelationship(DocumentRelationship dr) {
    if (dr == null) {
      return null;
    }

    return new DocumentRelationshipDto(
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
