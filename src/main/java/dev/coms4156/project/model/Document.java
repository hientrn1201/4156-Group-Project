package dev.coms4156.project.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
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
  @Column(name = "extracted_text")
  private String extractedText;

  @Lob
  @Column(name = "summary")
  private String summary;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status", nullable = false)
  private ProcessingStatus processingStatus;

  @CreationTimestamp
  @Column(name = "uploaded_at")
  private LocalDateTime uploadedAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Document() {
    this.processingStatus = ProcessingStatus.UPLOADED;
  }

  public enum ProcessingStatus {
    UPLOADED,
    TEXT_EXTRACTED,
    CHUNKED,
    EMBEDDINGS_GENERATED,
    SUMMARIZED,
    COMPLETED,
    FAILED
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public String getExtractedText() {
    return extractedText;
  }

  public void setExtractedText(String extractedText) {
    this.extractedText = extractedText;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public ProcessingStatus getProcessingStatus() {
    return processingStatus;
  }

  public void setProcessingStatus(ProcessingStatus processingStatus) {
    this.processingStatus = processingStatus;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}