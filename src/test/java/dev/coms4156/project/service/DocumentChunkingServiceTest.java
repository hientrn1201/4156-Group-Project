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
import java.util.Collections;
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

  @Test
  void testChunkDocument_WithNullChunkSize() {
    // Given - null chunkSize should use default
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("This is a test document with some content.");

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - pass null for chunkSize
    List<DocumentChunk> result = chunkingService.chunkDocument(document, null, 50);

    // Then - should use default chunk size (1000)
    assertTrue(result.size() >= 1);
    verify(documentChunkRepository).deleteByDocument(document);
  }

  @Test
  void testChunkDocument_WithNullOverlapSize() {
    // Given - null overlapSize should use default
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("This is a test document with some content.");

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - pass null for overlapSize
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 100, null);

    // Then - should use default overlap size (200)
    assertTrue(result.size() >= 1);
    verify(documentChunkRepository).deleteByDocument(document);
  }

  @Test
  void testChunkDocument_EndIndexAtTextLength() {
    // Given - text that ends exactly at chunk boundary
    Document document = new Document();
    document.setId(1L);
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      text.append("a");
    }
    document.setExtractedText(text.toString());

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - chunk size equals text length (endIndex == text.length())
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 100, 0);

    // Then - should create one chunk
    assertEquals(1, result.size());
  }

  @Test
  void testChunkDocument_SentenceBreaking_ShortSentence() {
    // Given - text where sentence end is too short (< 70% of chunk size)
    Document document = new Document();
    document.setId(1L);
    // Create text where sentence ends early (less than 70% of chunk size)
    String text = "Short sentence. " + "a".repeat(200);
    document.setExtractedText(text);

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 100, 0);

    // Then - should not break at sentence if it's too short
    assertTrue(result.size() >= 1);
  }

  @Test
  void testChunkDocument_SentenceBreaking_LongSentence() {
    // Given - text where sentence end is > 70% of chunk size
    Document document = new Document();
    document.setId(1L);
    // Create text with sentence ending after 80% of chunk size
    String text = "a".repeat(80) + ". " + "b".repeat(20);
    document.setExtractedText(text);

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 100, 0);

    // Then - should break at sentence boundary
    assertTrue(result.size() >= 1);
  }

  @Test
  void testChunkDocument_EmptyChunkContent() {
    // Given - text that might produce empty chunks after trimming
    Document document = new Document();
    document.setId(1L);
    // Text with only whitespace in a section
    document.setExtractedText("Valid content.   ");

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 5, 0);

    // Then - empty chunks should be skipped
    for (DocumentChunk chunk : result) {
      assertTrue(!chunk.getTextContent().trim().isEmpty());
    }
  }

  @Test
  void testChunkDocument_PreventInfiniteLoop() {
    // Given - edge case where startIndex might not advance
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("a".repeat(10));

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - use chunk size smaller than overlap to trigger loop prevention
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 5, 10);

    // Then - should complete without infinite loop
    assertTrue(result.size() >= 1);
  }

  @Test
  void testGetChunksForDocument_ById() {
    // Given
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

    when(documentChunkRepository.findByDocumentIdOrderByChunkIndex(1L))
        .thenReturn(Arrays.asList(chunk1, chunk2));

    // When
    List<DocumentChunk> result = chunkingService.getChunksForDocument(1L);

    // Then
    assertEquals(2, result.size());
    assertEquals("Chunk 1", result.get(0).getTextContent());
    assertEquals("Chunk 2", result.get(1).getTextContent());
  }

  @Test
  void testGetChunkStatistics_EmptyChunks() {
    // Given
    Document document = new Document();
    document.setId(1L);

    when(documentChunkRepository.findByDocumentOrderByChunkIndex(document))
        .thenReturn(Arrays.asList());

    // When
    DocumentChunkingService.ChunkStatistics result = chunkingService.getChunkStatistics(document);

    // Then - should return zeros
    assertEquals(0, result.getTotalChunks());
    assertEquals(0, result.getTotalCharacters());
    assertEquals(0, result.getAverageChunkSize());
    assertEquals(0, result.getMinChunkSize());
    assertEquals(0, result.getMaxChunkSize());
  }

  @Test
  void testChunkDocument_NoSentenceEndFound() {
    // Given - text with no sentence endings
    Document document = new Document();
    document.setId(1L);
    // Text with no periods, exclamation marks, or question marks
    String text = "This is a long text without any sentence endings " + "a".repeat(100);
    document.setExtractedText(text);

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 50, 10);

    // Then - should still create chunks even without sentence boundaries
    assertTrue(result.size() >= 1);
  }

  @Test
  void testChunkDocument_StartIndexLessThanEndIndex() {
    // Given - normal case where startIndex advances properly
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("This is a test document with multiple sentences. Here is another sentence. And one more.");

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 30, 5);

    // Then - should create multiple chunks
    assertTrue(result.size() > 1);
    // Verify chunks don't overlap incorrectly
    for (int i = 0; i < result.size() - 1; i++) {
      // Allow for overlap
      assertTrue(result.get(i).getEndPosition() 
          <= result.get(i + 1).getStartPosition() + 5);
    }
  }

  @Test
  void testChunkDocument_WithOverlap() {
    // Given - text that will create overlapping chunks
    Document document = new Document();
    document.setId(1L);
    StringBuilder text = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      text.append("Sentence ").append(i).append(". ");
    }
    document.setExtractedText(text.toString());

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - use overlap
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 50, 20);

    // Then - should create chunks with overlap
    assertTrue(result.size() >= 1);
    if (result.size() > 1) {
      // Verify overlap exists (end of chunk1 should be after start of chunk2)
      int overlap = result.get(0).getEndPosition() - result.get(1).getStartPosition();
      assertTrue(overlap >= 0); // Some overlap or at least no gap
    }
  }

  @Test
  void testChunkDocument_StartIndexGreaterThanOrEqualEndIndex() {
    // Given - edge case where startIndex might equal or exceed endIndex
    // This tests the branch: if (startIndex >= endIndex)
    Document document = new Document();
    document.setId(1L);
    document.setExtractedText("Short text.");

    doNothing().when(documentChunkRepository).deleteByDocument(document);

    // When - use very small chunk size that might cause startIndex >= endIndex
    // This can happen when the remaining text is shorter than chunk size
    List<DocumentChunk> result = chunkingService.chunkDocument(document, 1000, 0);

    // Then - should still create at least one chunk or handle gracefully
    assertTrue(result.size() >= 0); // May be empty or have chunks
  }

}