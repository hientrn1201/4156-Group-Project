package dev.coms4156.project.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for document search operations.
 * Contains search results and metadata about the search query.
 */
@Data
@AllArgsConstructor
@Builder
public class DocumentSearchResponse {
  private String query;

  private List<DocumentChunkDto> results;

  private Integer count;

  private String message;
}
