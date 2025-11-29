package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for document summary retrieval.
 * Contains the document ID and its generated summary.
 */
@Data
@AllArgsConstructor
@Builder
public class DocumentSummaryResponse {
  private Long documentId;

  private String summary;
}
