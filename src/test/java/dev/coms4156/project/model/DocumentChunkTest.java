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

  @Test
  void testEquals_DifferentDocuments() {
    Document doc1 = new Document();
    doc1.setId(1L);
    doc1.setFilename("doc1.pdf");
    
    Document doc2 = new Document();
    doc2.setId(2L);
    doc2.setFilename("doc2.pdf");
    
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setDocument(doc1);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setDocument(doc2);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setDocument(null);
    chunk2.setDocument(null);
    assertEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_NullVsNonNullDocument() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setDocument(doc);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setDocument(null);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_MetadataField() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setMetadata("meta1");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setMetadata("meta1");
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setMetadata("meta2");
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setMetadata(null);
    chunk2.setMetadata(null);
    assertEquals(chunk1, chunk2);
    
    chunk1.setMetadata("meta1");
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_DocumentIdField() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setDocumentId(1L);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setDocumentId(1L);
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setDocumentId(2L);
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setDocumentId(null);
    chunk2.setDocumentId(null);
    assertEquals(chunk1, chunk2);
    
    chunk1.setDocumentId(1L);
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_CreatedAtField() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    
    assertEquals(chunk1, chunk2);
    
    chunk1.setCreatedAt(java.time.LocalDateTime.now());
    chunk2.setCreatedAt(chunk1.getCreatedAt());
    assertEquals(chunk1, chunk2);
    
    chunk2.setCreatedAt(chunk1.getCreatedAt().plusSeconds(1));
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_UpdatedAtField() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    
    assertEquals(chunk1, chunk2);
    
    chunk1.setUpdatedAt(java.time.LocalDateTime.now());
    chunk2.setUpdatedAt(chunk1.getUpdatedAt());
    assertEquals(chunk1, chunk2);
    
    chunk2.setUpdatedAt(chunk1.getUpdatedAt().plusSeconds(1));
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_NullTimestampFields() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setCreatedAt(null);
    chunk1.setUpdatedAt(null);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setCreatedAt(null);
    chunk2.setUpdatedAt(null);
    
    assertEquals(chunk1, chunk2);
    
    chunk1.setCreatedAt(java.time.LocalDateTime.now());
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setCreatedAt(null);
    chunk1.setUpdatedAt(java.time.LocalDateTime.now());
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testHashCode_WithTimestamps() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .createdAt(java.time.LocalDateTime.now())
        .updatedAt(java.time.LocalDateTime.now())
        .build();
    
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .createdAt(chunk1.getCreatedAt())
        .updatedAt(chunk1.getUpdatedAt())
        .build();
    
    assertEquals(chunk1.hashCode(), chunk2.hashCode());
  }

  @Test
  void testEquals_RelationshipLists() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setSourceRelationships(null);
    chunk1.setTargetRelationships(null);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setSourceRelationships(null);
    chunk2.setTargetRelationships(null);
    
    assertEquals(chunk1, chunk2);
    
    // Test with empty lists
    chunk1.setSourceRelationships(java.util.Collections.emptyList());
    chunk2.setSourceRelationships(java.util.Collections.emptyList());
    assertEquals(chunk1, chunk2);
    
    // Test with different lists
    DocumentRelationship rel1 = new DocumentRelationship();
    rel1.setId(1L);
    chunk1.setSourceRelationships(java.util.Arrays.asList(rel1));
    chunk2.setSourceRelationships(java.util.Collections.emptyList());
    assertNotEquals(chunk1, chunk2);
    
    chunk1.setSourceRelationships(java.util.Collections.emptyList());
    chunk1.setTargetRelationships(java.util.Arrays.asList(rel1));
    chunk2.setTargetRelationships(java.util.Collections.emptyList());
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_AllFieldsCombined() {
    Document doc1 = new Document();
    doc1.setId(1L);
    Document doc2 = new Document();
    doc2.setId(1L);
    
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc1)
        .chunkIndex(0)
        .textContent("test")
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .documentId(1L)
        .embedding(new float[]{1.0f, 2.0f})
        .metadata("meta")
        .sourceRelationships(null)
        .targetRelationships(null)
        .build();
    
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(1L)
        .document(doc2)
        .chunkIndex(0)
        .textContent("test")
        .chunkSize(100)
        .startPosition(0)
        .endPosition(100)
        .documentId(1L)
        .embedding(new float[]{1.0f, 2.0f})
        .metadata("meta")
        .sourceRelationships(null)
        .targetRelationships(null)
        .build();
    
    assertEquals(chunk1, chunk2);
    assertEquals(chunk1.hashCode(), chunk2.hashCode());
  }

  @Test
  void testEquals_TextContentNull() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent(null);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent(null);
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setTextContent("test");
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_ChunkIndexNull() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(null);
    chunk1.setTextContent("test");
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(null);
    chunk2.setTextContent("test");
    
    assertEquals(chunk1, chunk2);
    
    chunk2.setChunkIndex(0);
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_EmbeddingArraysDifferentLengths() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setEmbedding(new float[]{1.0f, 2.0f});
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setEmbedding(new float[]{1.0f, 2.0f, 3.0f});
    
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_EmbeddingArraysSameLengthDifferentValues() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setEmbedding(new float[]{1.0f, 2.0f, 3.0f});
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setEmbedding(new float[]{1.0f, 2.0f, 4.0f});
    
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testEquals_OneNullOneEmptyRelationshipList() {
    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setId(1L);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("test");
    chunk1.setSourceRelationships(null);
    
    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setId(1L);
    chunk2.setChunkIndex(0);
    chunk2.setTextContent("test");
    chunk2.setSourceRelationships(java.util.Collections.emptyList());
    
    assertNotEquals(chunk1, chunk2);
  }

  @Test
  void testHashCode_WithRelationshipLists() {
    Document doc = new Document();
    doc.setId(1L);
    
    DocumentRelationship rel = new DocumentRelationship();
    rel.setId(1L);
    
    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .sourceRelationships(java.util.Arrays.asList(rel))
        .targetRelationships(java.util.Collections.emptyList())
        .build();
    
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(1L)
        .document(doc)
        .chunkIndex(0)
        .textContent("chunk")
        .sourceRelationships(java.util.Arrays.asList(rel))
        .targetRelationships(java.util.Collections.emptyList())
        .build();
    
    assertEquals(chunk1.hashCode(), chunk2.hashCode());
  }
}