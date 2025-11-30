package dev.coms4156.project.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DocumentChunkDTOTest {

  @Test
  void testFromDocumentChunk_ValidChunk() {
    DocumentChunk chunk = mock(DocumentChunk.class);
    Document doc = new Document();
    doc.setId(1L);
    
    when(chunk.getId()).thenReturn(1L);
    when(chunk.getChunkIndex()).thenReturn(0);
    when(chunk.getDocumentId()).thenReturn(1L);
    when(chunk.getTextContent()).thenReturn("Test content");
    when(chunk.getDocument()).thenReturn(doc);
    when(chunk.getSourceRelationships()).thenReturn(new ArrayList<>());
    when(chunk.getTargetRelationships()).thenReturn(new ArrayList<>());

    try (MockedStatic<Hibernate> hibernateMock = Mockito.mockStatic(Hibernate.class)) {
      hibernateMock.when(() -> Hibernate.isInitialized(doc)).thenReturn(true);
      hibernateMock.when(() -> Hibernate.isInitialized(new ArrayList<>())).thenReturn(true);

      DocumentChunkDTO dto = DocumentChunkDTO.fromDocumentChunk(chunk);

      assertNotNull(dto);
      assertEquals(1L, dto.getId());
      assertEquals("Test content", dto.getTextContent());
    }
  }

  @Test
  void testFromDocumentChunk_NullChunk() {
    DocumentChunkDTO dto = DocumentChunkDTO.fromDocumentChunk(null);
    assertNull(dto);
  }

  @Test
  void testFromDocumentChunk_UninitializedCollections() {
    DocumentChunk chunk = mock(DocumentChunk.class);
    
    when(chunk.getId()).thenReturn(1L);
    when(chunk.getTextContent()).thenReturn("Test content");
    when(chunk.getDocument()).thenReturn(null);
    when(chunk.getSourceRelationships()).thenReturn(new ArrayList<>());
    when(chunk.getTargetRelationships()).thenReturn(new ArrayList<>());

    try (MockedStatic<Hibernate> hibernateMock = Mockito.mockStatic(Hibernate.class)) {
      hibernateMock.when(() -> Hibernate.isInitialized(new ArrayList<>())).thenReturn(false);

      DocumentChunkDTO dto = DocumentChunkDTO.fromDocumentChunk(chunk);

      assertNotNull(dto);
      assertNull(dto.getSourceRelationships());
      assertNull(dto.getTargetRelationships());
    }
  }

  @Test
  void testBuilder() {
    DocumentChunkDTO dto = DocumentChunkDTO.builder()
        .id(1L)
        .textContent("Test")
        .build();

    assertEquals(1L, dto.getId());
    assertEquals("Test", dto.getTextContent());
  }
}