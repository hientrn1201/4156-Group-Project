package dev.coms4156.project.dtos;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.Document.ProcessingStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for Document entities.
 * Used to transfer document data between layers.
 */
// CHECKSTYLE.OFF: AbbreviationAsWordInName
@Data
@AllArgsConstructor
@Builder
public class DocumentDto { // CHECKSTYLE.ON: AbbreviationAsWordInName
  private Long id;

  private String filename;

  private String contentType;

  private Long fileSize;

  private ProcessingStatus processingStatus;

  private String summary;

  private LocalDateTime uploadedAt;

  private LocalDateTime updatedAt;

  /**
   * Converts a Document entity to a DocumentDto.
   *
   * @param doc the Document entity to convert
   * @return the DocumentDto, or null if the input is null
   */
  public static DocumentDto fromDocument(Document doc) {

    if (doc == null) {
      return null;
    }

    return new DocumentDto(
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
