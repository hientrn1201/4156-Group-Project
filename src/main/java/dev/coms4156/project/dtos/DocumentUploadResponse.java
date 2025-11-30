package dev.coms4156.project.dtos;

import dev.coms4156.project.model.Document.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for document upload operations.
 * Contains information about the uploaded document.
 */
@Data
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
  private Long documentId;

  private String filename;

  private ProcessingStatus processingStatus;

  private String message;
}
