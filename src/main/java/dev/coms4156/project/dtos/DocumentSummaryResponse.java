package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentSummaryResponse {
  private Long documentId;

  private String summary;
}
