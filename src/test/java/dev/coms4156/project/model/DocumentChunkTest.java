package dev.coms4156.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentChunkTest {

  @Test
  void testEqualsAndHashCode() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setTextContent("test");
    
    DocumentChunk chunk3 = new DocumentChunk();
    chunk3.setId(2L);
    chunk3.setTextContent("different");
    
    assertEquals(chunk1, chunk2);
    assertEquals(chunk1.hashCode(), chunk2.hashCode());
    assertNotEquals(chunk1, chunk3);
    assertNotEquals(chunk1, null);
    assertNotEquals(chunk1, "string");
    assertEquals(chunk1, chunk1);
  }

  @Test
  void testToString() {
    DocumentChunk chunk = new DocumentChunk();
    chunk.setId(1L);
    chunk.setTextContent("test");
    String result = chunk.toString();
    assertNotNull(result);
    assertTrue(result.contains("DocumentChunk"));
  }

  @Test
  void testEquals_NullFields() {
    DocumentChunk chunk1 = new DocumentChunk();
    DocumentChunk chunk2 = new DocumentChunk();
    
    assertEquals(chunk1, chunk2);
    
    chunk1.setId(null);
    chunk2.setId(1L);
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_ComplexFields() {
    Document doc1 = new Document();
    doc1.setId(1L);
    Document doc2 = new Document();
    doc2.setId(2L);
    
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(doc1);
    chunk1.setEmbedding(new float[]{1.0f, 2.0f});
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(doc2);
    chunk2.setEmbedding(new float[]{3.0f, 4.0f});
    
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testHashCode_NullFields() {
    DocumentChunk chunk = new DocumentChunk();
    chunk.setId(null);
    chunk.setTextContent(null);
    
    int hash = chunk.hashCode();
    assertNotNull(hash);
  }
}