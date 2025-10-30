package dev.coms4156.project.dtos;

import java.time.LocalDateTime;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.Document.ProcessingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentDTO {
  private Long id;

  private String filename;

  private String contentType;

  private Long fileSize;

  private ProcessingStatus processingStatus;

  private String summary;

  private LocalDateTime uploadedAt;

  private LocalDateTime updatedAt;

  public static DocumentDTO fromDocument(Document doc) {

    if (doc == null) {
      return null;
    }

    return new DocumentDTO(
        doc.getId(),
        doc.getFilename(),
        doc.getContentType(),
        doc.getFileSize(),
        doc.getProcessingStatus(),
        doc.getSummary(),
        doc.getUploadedAt(),
        doc.getUpdatedAt());
  }
}
