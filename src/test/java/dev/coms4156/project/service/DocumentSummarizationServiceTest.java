package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.repository.DocumentRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class DocumentSummarizationServiceTest {

  @Mock
  private DocumentRepository documentRepository;

  private DocumentSummarizationService summarizationService;

  @Mock
  private ChatClient chatClient;

  @BeforeEach
  void setUp() {
    summarizationService = new DocumentSummarizationService(documentRepository, chatClient);
  }

  @Test
  void testGenerateSummary() {
    // Given
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText("This is a long text that needs to be summarized. "
        + "It contains multiple sentences and should be truncated to 200 characters. "
        + "This text is longer than 200 characters to test the summarization logic.");

    when(documentRepository.save(any(Document.class))).thenReturn(document);

    // When
    String result = summarizationService.generateSummary(document);

    // Then
    assertNotNull(result);
    assertTrue(result.length() > 0);
    verify(documentRepository).save(document);
  }

  @Test
  void testGenerateSimpleSummary() {
    // Given
    String longText = "This is a very long text that exceeds 200 characters. "
        + "It should be truncated to exactly 200 characters and have ellipsis added. "
        + "This ensures the summary is concise and readable for users.";

    // When
    String result = summarizationService.generateSimpleSummary(longText);

    // Then
    assertNotNull(result);
    assertTrue(result.length() > 0);
  }

  @Test
  void testGetDocumentSummary() {
    // Given
    Document document = new Document();
    document.setId(1L);
    document.setSummary("Test summary");
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

    // When
    String result = summarizationService.getDocumentSummary(1L);

    // Then
    assertEquals("Test summary", result);
  }

  @Test
  void testGenerateAiSummary() {
    // Given
    String inputText = "Machine learning is a subset of artificial intelligence that focuses"
        + " on algorithms and statistical models.";

    // When - Call AI summarization (will fallback if AI not available)
    String result = summarizationService.generateAiSummary(inputText);

    // Then - Should return a valid summary
    assertNotNull(result);
    assertTrue(result.length() > 0);
    assertTrue(result.contains("Machine learning"));
  }

  @Test
  void testGenerateSummary_NullText() {
    // Given - document with null extracted text
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText(null);

    // When
    String result = summarizationService.generateSummary(document);

    // Then - should return null
    assertNull(result);
  }

  @Test
  void testGenerateSummary_EmptyText() {
    // Given - document with empty extracted text
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText("   "); // Whitespace only

    // When
    String result = summarizationService.generateSummary(document);

    // Then - should return null
    assertNull(result);
  }

  @Test
  void testGenerateSummary_ExceptionDuringGeneration() {
    // Given - AI summary generation throws exception, but generateAiSummary catches it
    // and falls back to simple summary, so generateSummary should succeed
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText("Sample text");

    when(chatClient.prompt()).thenThrow(new RuntimeException("AI service unavailable"));
    when(documentRepository.save(any(Document.class))).thenReturn(document);

    // When - generateAiSummary will catch exception and fallback, so generateSummary succeeds
    String result = summarizationService.generateSummary(document);

    // Then - should return fallback summary (not null)
    assertNotNull(result);
    assertEquals("Sample text", result); // Simple summary for short text
  }

  @Test
  void testGenerateSimpleSummary_ShortText() {
    // Given - text is <= 200 characters
    String shortText = "This is a short text.";

    // When
    String result = summarizationService.generateSimpleSummary(shortText);

    // Then - should return text as-is
    assertEquals(shortText, result);
  }

  @Test
  void testGenerateSimpleSummary_Exactly200Characters() {
    // Given - text is exactly 200 characters
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 200; i++) {
      sb.append("a");
    }
    String exactText = sb.toString();

    // When
    String result = summarizationService.generateSimpleSummary(exactText);

    // Then - should return text as-is (no truncation)
    assertEquals(200, result.length());
    assertEquals(exactText, result);
  }

  @Test
  void testGenerateSimpleSummary_LongText() {
    // Given - text is > 200 characters
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 300; i++) {
      sb.append("a");
    }
    String longText = sb.toString();

    // When
    String result = summarizationService.generateSimpleSummary(longText);

    // Then - should truncate to 200 + "..."
    assertEquals(203, result.length()); // 200 + "..."
    assertNotNull(result);
  }

  @Test
  void testGetDocumentSummary_DocumentNotFound() {
    // Given - document doesn't exist
    when(documentRepository.findById(999L)).thenReturn(Optional.empty());

    // When
    String result = summarizationService.getDocumentSummary(999L);

    // Then - should return null
    assertNull(result);
  }

  @Test
  void testGetDocumentSummary_DocumentWithoutSummary() {
    // Given - document exists but has no summary
    Document document = new Document();
    document.setId(1L);
    document.setSummary(null);
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

    // When
    String result = summarizationService.getDocumentSummary(1L);

    // Then - should return null
    assertNull(result);
  }

  @Test
  void testGenerateAiSummary_ExceptionFallsBackToSimple() {
    // Given - AI generation fails
    String text = "Sample text for summarization";
    when(chatClient.prompt()).thenThrow(new RuntimeException("AI unavailable"));

    // When - should fallback to simple summary
    String result = summarizationService.generateAiSummary(text);

    // Then - should return simple summary (not null)
    assertNotNull(result);
    assertEquals(text, result); // Simple summary for short text
  }

}