package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for document statistics.
 * Contains overall statistics about documents in the system.
 */
@Data
@AllArgsConstructor
@Builder
public class DocumentStatsResponse {
  private Long total;

  private DocumentStatusCounts byStatus;

  private Double completionRate;

  private Double failureRate;
}