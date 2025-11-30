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

  @Test
  void testEquals_AllFieldsDifferent() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk1")
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .documentId(1L)
        .build();
    
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk1")
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .documentId(1L)
        .build();
    
    assertEquals(chunk1, chunk2);
    
    // Test each field difference
    chunk2.setChunkIndex(1);
    assertNotEquals(chunk1, chunk2);
    
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("chunk2");
    assertNotEquals(chunk1, chunk2);
    
    chunk2.setTextContent("chunk1");
    chunk2.setChunkSize(200);
    assertNotEquals(chunk1, chunk2);
    
    chunk2.setChunkSize(100);
    chunk2.setStartPosition(10);
    assertNotEquals(chunk1, chunk2);
    
    chunk2.setStartPosition(0);
    chunk2.setEndPosition(200);
    assertNotEquals(chunk1, chunk2);
    
    chunk2.setEndPosition(100);
    chunk2.setDocumentId(2L);
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_NullableFields() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setTextContent("test");
    chunk1.setChunkIndex(0);
    chunk1.setChunkSize(null);
    chunk1.setStartPosition(null);
    chunk1.setEndPosition(null);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setTextContent("test");
    chunk2.setChunkIndex(0);
    chunk2.setChunkSize(null);
    chunk2.setStartPosition(null);
    chunk2.setEndPosition(null);
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setChunkSize(100);
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setChunkSize(100);
    chunk2.setStartPosition(10);
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setStartPosition(10);
    chunk2.setEndPosition(110);
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_EmbeddingArrays() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setTextContent("test");
    chunk1.setChunkIndex(0);
    chunk1.setEmbedding(new float[]{1.0f, 2.0f, 3.0f});
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setTextContent("test");
    chunk2.setChunkIndex(0);
    chunk2.setEmbedding(new float[]{1.0f, 2.0f, 3.0f});
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setEmbedding(new float[]{4.0f, 5.0f, 6.0f});
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setEmbedding(null);
    chunk2.setEmbedding(null);
    assertEquals(chunk1, chunk2);
  }

  @Test
  void testHashCode_Consistency() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .chunkSize(100)
        .build();
    
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .chunkSize(100)
        .build();
    
    assertEquals(chunk1.hashCode(), chunk2.hashCode());
  }

  @Test
  void testBuilder_WithAllFields() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentChunk chunk = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .documentId(1L)
        .embedding(new float[]{1.0f, 2.0f})
        .metadata("{\"key\":\"value\"}")
        .build();
    
    assertNotNull(chunk);
    assertEquals(1L, chunk.getId());
    assertEquals(0, chunk.getChunkIndex());
    assertEquals("chunk", chunk.getTextContent());
    assertEquals(100, chunk.getChunkSize());
    assertEquals(0, chunk.getStartPosition());
    assertEquals(100, chunk.getEndPosition());
    assertEquals(1L, chunk.getDocumentId());
    assertNotNull(chunk.getEmbedding());
    assertEquals("{\"key\":\"value\"}", chunk.getMetadata());
  }
}