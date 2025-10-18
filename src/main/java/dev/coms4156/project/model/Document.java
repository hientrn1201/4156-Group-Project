package dev.coms4156.project.model;

import jakarta.persistence.*;


import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name ="documents")
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="filename")
  private String filename;

  @Column(name="type")
  private String contentType;

  @Column(name="file_size")
  private Long fileSize;
  @Lob

  @Column(name="text")
  private String extractedText;

  @CreationTimestamp
  @Column(name="uploaded_at")
  private LocalDateTime uploadedAt;
  public Document() {}               

  public Long getId() {
    return id; 
  }

  public void setId(Long id) 
  { 
    this.id = id; }
  public String getFilename(){
     return filename; 
    
    }
  public void setFilename(String filename) { 
    this.filename =filename; }

  public String getContentType() { 
    return contentType; }

  public void setContentType(String contentType) { 
    this.contentType =contentType; }

  public Long getFileSize() { 
    return fileSize; }
  public void setFileSize(Long fileSize) {
     this.fileSize =fileSize; }
  public String getExtractedText() { 
    return extractedText; }
  public void setExtractedText(String extractedText) { 
    this.extractedText=extractedText; 
  }
  public LocalDateTime getUploadedAt() { 
    return uploadedAt; 
  }
  public void setUploadedAt(LocalDateTime uploadedAt) { 
    this.uploadedAt=uploadedAt; 
  }
}
