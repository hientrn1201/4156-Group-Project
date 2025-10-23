package dev.coms4156.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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
    assertEquals("COMPLETED", Document.ProcessingStatus.COMPLETED.toString());
    assertEquals("FAILED", Document.ProcessingStatus.FAILED.toString());
  }
}