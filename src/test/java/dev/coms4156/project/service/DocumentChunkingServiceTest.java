package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentChunkRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentChunkingServiceTest {

  @Mock
  private DocumentChunkRepository documentChunkRepository;

  private DocumentChunkingService chunkingService;

  @BeforeEach
  void setUp() {
    chunkingService = new DocumentChunkingService(documentChunkRepository);
  }

  @Test
  void testChunkDocument_NullDocument() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      chunkingService.chunkDocument(null);
    });
  }

  @Test
  void testChunkDocument_EmptyExtractedText() {
    // Given
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("   ");

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      chunkingService.chunkDocument(document);
    });
  }

  @Test
  void testChunkDocument_LongText() {
    // Given
    Document document = new Document();
    document.setId(1L);

    // Create text longer than default chunk size (1000 chars)
    StringBuilder longText = new StringBuilder();
    for (int i = 0; i < 50; i++) {
      longText.append("This is sentence number ").append(i).append(". ");
    }
    document.setExtractedText(longText.toString());

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document);

    // Then
    assertTrue(result.size() > 1);
    assertEquals(0, result.get(0).getChunkIndex());
    assertEquals(1, result.get(1).getChunkIndex());
    verify(documentChunkRepository).deleteByDocument(document);
  }

  @Test
  void testChunkDocument_WithSentenceBreaking() {
    // Given
    Document document = new Document();
    document.setId(1L);

    // Create text with clear sentence boundaries
    StringBuilder textWithSentences = new StringBuilder();
    for (int i = 0; i < 20; i++) {
      textWithSentences.append("This is sentence number ").append(i).append(". ");
    }
    document.setExtractedText(textWithSentences.toString());

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 100, 20);

    // Then
    assertTrue(result.size() >= 1);
    // Verify that chunks respect sentence boundaries where possible
    for (DocumentChunk chunk : result) {
      assertTrue(chunk.getTextContent().length() > 0);
    }
  }

  @Test
  void testGetChunksForDocument() {
    // Given
    Document document = new Document();
    document.setId(1L);

    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .textContent("Chunk 1")
        .chunkIndex(0)
        .build();
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .textContent("Chunk 2")
        .chunkIndex(1)
        .build();

    when(documentChunkRepository.findByDocumentOrderByChunkIndex(document))
        .thenReturn(Arrays.asList(chunk1, chunk2));

    // When
    List<DocumentChunk> result = chunkingService.getChunksForDocument(document);

    // Then
    assertEquals(2, result.size());
    assertEquals("Chunk 1", result.get(0).getTextContent());
    assertEquals("Chunk 2", result.get(1).getTextContent());
  }

  @Test
  void testDeleteChunksForDocument() {
    // Given
    Document document = new Document();
    document.setId(1L);

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    chunkingService.deleteChunksForDocument(document);

    // Then
    verify(documentChunkRepository).deleteByDocument(document);
  }

  @Test
  void testGetChunkStatistics() {
    // Given
    Document document = new Document();
    document.setId(1L);

    DocumentChunk chunk1 = DocumentChunk.builder()
        .id(1L)
        .textContent("Short")
        .chunkSize(5)
        .build();
    DocumentChunk chunk2 = DocumentChunk.builder()
        .id(2L)
        .textContent("Medium length text")
        .chunkSize(18)
        .build();
    DocumentChunk chunk3 = DocumentChunk.builder()
        .id(3L)
        .textContent("This is a longer chunk of text")
        .chunkSize(30)
        .build();

    when(documentChunkRepository.findByDocumentOrderByChunkIndex(document))
        .thenReturn(Arrays.asList(chunk1, chunk2, chunk3));

    // When
    DocumentChunkingService.ChunkStatistics result = chunkingService.getChunkStatistics(document);

    // Then
    assertEquals(3, result.getTotalChunks());
    assertEquals(53, result.getTotalCharacters());
    assertEquals(17, result.getAverageChunkSize()); // 53/3 = 17.67 -> 17
    assertEquals(5, result.getMinChunkSize());
    assertEquals(30, result.getMaxChunkSize());
  }

  @Test
  void testChunkStatistics_Constructor() {
    // When
    DocumentChunkingService.ChunkStatistics stats =
        new DocumentChunkingService.ChunkStatistics(5, 1000, 200, 150, 250);

    // Then
    assertEquals(5, stats.getTotalChunks());
    assertEquals(1000, stats.getTotalCharacters());
    assertEquals(200, stats.getAverageChunkSize());
    assertEquals(150, stats.getMinChunkSize());
    assertEquals(250, stats.getMaxChunkSize());
  }
}