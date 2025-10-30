package dev.coms4156.project.dtos;

import dev.coms4156.project.model.Document.ProcessingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
  private Long documentId;

  private String filename;

  private ProcessingStatus processingStatus;

  private String message;
}
