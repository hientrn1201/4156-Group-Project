package dev.coms4156.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a document with extracted text and processing status.
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "filename", nullable = false)
  private String filename;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "file_size")
  private Long fileSize;

  @Lob
  @Column(name = "extracted_text", columnDefinition = "TEXT")
  @JsonIgnore
  private String extractedText;

  @Lob
  @Column(name = "summary", columnDefinition = "TEXT")
  @JsonIgnore
  private String summary;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status", nullable = false)
  private ProcessingStatus processingStatus = ProcessingStatus.UPLOADED;

  @CreationTimestamp
  @Column(name = "uploaded_at")
  private LocalDateTime uploadedAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Enum representing the processing status of a document.
   */
  public enum ProcessingStatus {
    UPLOADED,
    TEXT_EXTRACTED,
    CHUNKED,
    EMBEDDINGS_GENERATED,
    SUMMARIZED,
    COMPLETED,
    FAILED
  }
}
