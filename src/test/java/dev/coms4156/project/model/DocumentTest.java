package dev.coms4156.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentTest {

  @Test
  void testEqualsAndHashCode() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    
    Document doc3 = new Document();
    doc3.setId(2L);
    doc3.setFilename("other.pdf");
    
    assertEquals(doc1, doc2);
    assertEquals(doc1.hashCode(), doc2.hashCode());
    assertNotEquals(doc1, doc3);
    assertNotEquals(doc1, null);
    assertNotEquals(doc1, "string");
    assertEquals(doc1, doc1);
  }

  @Test
  void testEquals_NullFields() {
    Document doc1 = new Document();
    Document doc2 = new Document();
    
    assertEquals(doc1, doc2);
    
    doc1.setId(null);
    doc2.setId(1L);
    assertNotEquals(doc1, doc2);
    
    doc1.setId(1L);
    doc1.setFilename(null);
    doc2.setFilename("test.pdf");
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testHashCode_NullFields() {
    Document doc = new Document();
    doc.setId(null);
    doc.setFilename(null);
    
    int hash = doc.hashCode();
    assertNotNull(hash);
  }

  @Test
  void testToString() {
    Document doc = new Document();
    doc.setId(1L);
    doc.setFilename("test.pdf");
    
    String result = doc.toString();
    assertNotNull(result);
    assertTrue(result.contains("Document"));
  }

  @Test
  void testProcessingStatusEnum() {
    assertEquals("UPLOADED", Document.ProcessingStatus.UPLOADED.toString());
    assertEquals("TEXT_EXTRACTED", Document.ProcessingStatus.TEXT_EXTRACTED.toString());
    assertEquals("CHUNKED", Document.ProcessingStatus.CHUNKED.toString());
    assertEquals("EMBEDDINGS_GENERATED", Document.ProcessingStatus.EMBEDDINGS_GENERATED.toString());
    assertEquals("SUMMARIZED", Document.ProcessingStatus.SUMMARIZED.toString());
    assertEquals("COMPLETED", Document.ProcessingStatus.COMPLETED.toString());
    assertEquals("FAILED", Document.ProcessingStatus.FAILED.toString());
  }

  @Test
  void testEquals_AllFieldsDifferent() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .fileSize(1024L)
        .processingStatus(Document.ProcessingStatus.COMPLETED)
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .fileSize(1024L)
        .processingStatus(Document.ProcessingStatus.COMPLETED)
        .build();
    
    assertEquals(doc1, doc2);
    
    // Test each field difference
    doc2.setContentType("text/plain");
    assertNotEquals(doc1, doc2);
    
    doc2.setContentType("application/pdf");
    doc2.setFileSize(2048L);
    assertNotEquals(doc1, doc2);
    
    doc2.setFileSize(1024L);
    doc2.setProcessingStatus(Document.ProcessingStatus.FAILED);
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testEquals_NullableFields() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    doc1.setContentType("application/pdf");
    doc1.setFileSize(null);
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    doc2.setContentType("application/pdf");
    doc2.setFileSize(null);
    
    assertEquals(doc1, doc2);
    
    doc2.setFileSize(1024L);
    assertNotEquals(doc1, doc2);
    
    doc1.setFileSize(1024L);
    assertEquals(doc1, doc2);
  }

  @Test
  void testHashCode_Consistency() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .fileSize(1024L)
        .processingStatus(Document.ProcessingStatus.COMPLETED)
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .fileSize(1024L)
        .processingStatus(Document.ProcessingStatus.COMPLETED)
        .build();
    
    assertEquals(doc1.hashCode(), doc2.hashCode());
  }

  @Test
  void testBuilder_WithAllFields() {
    Document doc = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .fileSize(1024L)
        .extractedText("Sample text")
        .summary("Sample summary")
        .processingStatus(Document.ProcessingStatus.COMPLETED)
        .build();
    
    assertNotNull(doc);
    assertEquals(1L, doc.getId());
    assertEquals("test.pdf", doc.getFilename());
    assertEquals("application/pdf", doc.getContentType());
    assertEquals(1024L, doc.getFileSize());
    assertEquals("Sample text", doc.getExtractedText());
    assertEquals("Sample summary", doc.getSummary());
    assertEquals(Document.ProcessingStatus.COMPLETED, doc.getProcessingStatus());
  }

  @Test
  void testEquals_ExtractedTextAndSummary() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .extractedText("Text 1")
        .summary("Summary 1")
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .extractedText("Text 1")
        .summary("Summary 1")
        .build();
    
    Document doc3 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .extractedText("Text 2")
        .summary("Summary 1")
        .build();
    
    assertEquals(doc1, doc2);
    assertNotEquals(doc1, doc3);
    
    doc1.setExtractedText(null);
    doc2.setExtractedText("Text 1");
    assertNotEquals(doc1, doc2);
    
    doc1.setExtractedText("Text 1");
    doc1.setSummary(null);
    doc2.setSummary("Summary 1");
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testEquals_AllStatusValues() {
    Document base = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .processingStatus(Document.ProcessingStatus.UPLOADED)
        .build();
    
    for (Document.ProcessingStatus status : Document.ProcessingStatus.values()) {
      Document doc = Document.builder()
          .id(1L)
          .filename("test.pdf")
          .contentType("application/pdf")
          .processingStatus(status)
          .build();
      
      if (status == Document.ProcessingStatus.UPLOADED) {
        assertEquals(base, doc);
      } else {
        assertNotEquals(base, doc);
      }
    }
  }

  @Test
  void testEquals_UploadedAtField() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .build();
    
    assertEquals(doc1, doc2);
    
    doc1.setUploadedAt(java.time.LocalDateTime.now());
    doc2.setUploadedAt(java.time.LocalDateTime.now().plusSeconds(1));
    // Timestamps might be different, but equals should still work if other fields match
    // Actually, Lombok's @Data includes all fields in equals, so timestamps matter
    assertNotEquals(doc1, doc2);
    
    doc2.setUploadedAt(doc1.getUploadedAt());
    assertEquals(doc1, doc2);
  }

  @Test
  void testEquals_UpdatedAtField() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .build();
    
    assertEquals(doc1, doc2);
    
    doc1.setUpdatedAt(java.time.LocalDateTime.now());
    doc2.setUpdatedAt(java.time.LocalDateTime.now().plusSeconds(1));
    assertNotEquals(doc1, doc2);
    
    doc2.setUpdatedAt(doc1.getUpdatedAt());
    assertEquals(doc1, doc2);
  }

  @Test
  void testEquals_NullTimestampFields() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    doc1.setContentType("application/pdf");
    doc1.setUploadedAt(null);
    doc1.setUpdatedAt(null);
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    doc2.setContentType("application/pdf");
    doc2.setUploadedAt(null);
    doc2.setUpdatedAt(null);
    
    assertEquals(doc1, doc2);
    
    doc1.setUploadedAt(java.time.LocalDateTime.now());
    assertNotEquals(doc1, doc2);
    
    doc1.setUploadedAt(null);
    doc1.setUpdatedAt(java.time.LocalDateTime.now());
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testHashCode_WithTimestamps() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .uploadedAt(java.time.LocalDateTime.now())
        .updatedAt(java.time.LocalDateTime.now())
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .uploadedAt(doc1.getUploadedAt())
        .updatedAt(doc1.getUpdatedAt())
        .build();
    
    assertEquals(doc1.hashCode(), doc2.hashCode());
  }

  @Test
  void testEquals_FilenameNull() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename(null);
    doc1.setContentType("application/pdf");
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename(null);
    doc2.setContentType("application/pdf");
    
    assertEquals(doc1, doc2);
    
    doc2.setFilename("test.pdf");
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testEquals_ContentTypeNull() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    doc1.setContentType(null);
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    doc2.setContentType(null);
    
    assertEquals(doc1, doc2);
    
    doc2.setContentType("application/pdf");
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testEquals_AllFieldsWithNulls() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    doc1.setContentType("application/pdf");
    doc1.setFileSize(null);
    doc1.setExtractedText(null);
    doc1.setSummary(null);
    doc1.setUploadedAt(null);
    doc1.setUpdatedAt(null);
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    doc2.setContentType("application/pdf");
    doc2.setFileSize(null);
    doc2.setExtractedText(null);
    doc2.setSummary(null);
    doc2.setUploadedAt(null);
    doc2.setUpdatedAt(null);
    
    assertEquals(doc1, doc2);
    assertEquals(doc1.hashCode(), doc2.hashCode());
  }

  @Test
  void testHashCode_WithAllNullFields() {
    Document doc = new Document();
    doc.setId(null);
    doc.setFilename(null);
    doc.setContentType(null);
    doc.setFileSize(null);
    doc.setExtractedText(null);
    doc.setSummary(null);
    doc.setUploadedAt(null);
    doc.setUpdatedAt(null);
    
    int hash = doc.hashCode();
    assertNotNull(hash);
  }

  @Test
  void testEquals_ProcessingStatusDefault() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .processingStatus(Document.ProcessingStatus.UPLOADED)
        .build();
    
    assertEquals(doc1, doc2);
  }

  @Test
  void testEquals_ExtractedTextAndSummaryCombinations() {
    Document doc1 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .extractedText("text1")
        .summary("summary1")
        .build();
    
    Document doc2 = Document.builder()
        .id(1L)
        .filename("test.pdf")
        .contentType("application/pdf")
        .extractedText("text1")
        .summary("summary1")
        .build();
    
    assertEquals(doc1, doc2);
    
    doc2.setExtractedText("text2");
    assertNotEquals(doc1, doc2);
    
    doc2.setExtractedText("text1");
    doc2.setSummary("summary2");
    assertNotEquals(doc1, doc2);
  }

  @Test
  void testHashCode_ConsistencyWithNulls() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("test.pdf");
    doc1.setContentType("application/pdf");
    doc1.setFileSize(null);
    doc1.setExtractedText(null);
    doc1.setSummary(null);
    
    Document doc2 = new Document();
    doc2.setId(1L);
    doc2.setFilename("test.pdf");
    doc2.setContentType("application/pdf");
    doc2.setFileSize(null);
    doc2.setExtractedText(null);
    doc2.setSummary(null);
    
    assertEquals(doc1.hashCode(), doc2.hashCode());
  }
}