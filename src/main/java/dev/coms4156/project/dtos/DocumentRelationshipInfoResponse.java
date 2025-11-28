package dev.coms4156.project.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for document relationship information.
 * Contains relationships associated with a document.
 */
@Data
@AllArgsConstructor
@Builder
public class DocumentRelationshipInfoResponse {
  private Long documentId;

  // TODO: change this to some real relationship object
  private List<DocumentRelationshipDTO> relationships;

  private Integer count;

  private String message;
}
