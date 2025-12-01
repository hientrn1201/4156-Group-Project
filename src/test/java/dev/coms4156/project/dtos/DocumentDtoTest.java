package dev.coms4156.project.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.coms4156.project.model.Document;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DocumentDtoTest {

  @Test
  void testFromDocument_ValidDocument() {
    Document doc = new Document();
    doc.setId(1L);
    doc.setFilename("test.pdf");
    doc.setContentType("application/pdf");
    doc.setFileSize(1024L);
    doc.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    doc.setSummary("Test summary");
    doc.setUploadedAt(LocalDateTime.now());
    doc.setUpdatedAt(LocalDateTime.now());

    DocumentDto dto = DocumentDto.fromDocument(doc);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("test.pdf", dto.getFilename());
    assertEquals("application/pdf", dto.getContentType());
  }

  @Test
  void testFromDocument_NullDocument() {
    DocumentDto dto = DocumentDto.fromDocument(null);
    assertNull(dto);
  }

  @Test
  void testBuilder() {
    DocumentDto dto = DocumentDto.builder()
        .id(1L)
        .filename("test.pdf")
        .build();

    assertEquals(1L, dto.getId());
    assertEquals("test.pdf", dto.getFilename());
  }
}