package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

  // Invalid equivalence partition - null extracted text
  @Test
  void testGenerateSummary_NullText() {
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText(null);

    String result = summarizationService.generateSummary(document);

    assertEquals(null, result);
  }

  // Invalid equivalence partition - empty extracted text
  @Test
  void testGenerateSummary_EmptyText() {
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText("");

    String result = summarizationService.generateSummary(document);

    assertEquals(null, result);
  }

  // Invalid equivalence partition - whitespace-only text
  @Test
  void testGenerateSummary_WhitespaceOnlyText() {
    Document document = new Document();
    document.setId(1L);
    document.setFilename("test.pdf");
    document.setExtractedText("   \n\t   ");

    String result = summarizationService.generateSummary(document);

    assertEquals(null, result);
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

  // Boundary analysis - very short text (under 200 chars)
  @Test
  void testGenerateSimpleSummary_ShortText() {
    String shortText = "This is a short document.";

    String result = summarizationService.generateSimpleSummary(shortText);

    assertEquals(shortText, result);
  }

  // Boundary analysis - exactly 200 characters
  @Test
  void testGenerateSimpleSummary_Exactly200Chars() {
    String text200 = "a".repeat(200);

    String result = summarizationService.generateSimpleSummary(text200);

    assertEquals(text200, result);
  }

  // Boundary analysis - 201 characters (just over limit)
  @Test
  void testGenerateSimpleSummary_Over200Chars() {
    String text201 = "a".repeat(201);

    String result = summarizationService.generateSimpleSummary(text201);

    assertEquals("a".repeat(200) + "...", result);
    assertEquals(203, result.length()); // 200 + "..."
  }

  // Valid equivalence partition - very long text
  @Test
  void testGenerateSimpleSummary_VeryLongText() {
    String longText = "This is a very long document. ".repeat(100);

    String result = summarizationService.generateSimpleSummary(longText);

    assertEquals(203, result.length()); // 200 + "..."
    assertTrue(result.endsWith("..."));
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

  // Invalid equivalence partition - document not found
  @Test
  void testGetDocumentSummary_NotFound() {
    Long documentId = 999L;

    when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

    String result = summarizationService.getDocumentSummary(documentId);

    assertEquals(null, result);
  }

  // Invalid equivalence partition - document exists but no summary
  @Test
  void testGetDocumentSummary_NoSummary() {
    Long documentId = 1L;
    Document document = new Document();
    document.setId(documentId);
    document.setSummary(null);

    when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

    String result = summarizationService.getDocumentSummary(documentId);

    assertEquals(null, result);
  }

  // Boundary analysis - minimum valid document ID
  @Test
  void testGetDocumentSummary_MinimumId() {
    Long documentId = 1L;
    Document document = new Document();
    document.setId(documentId);
    document.setSummary("Summary for doc 1");

    when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

    String result = summarizationService.getDocumentSummary(documentId);

    assertEquals("Summary for doc 1", result);
  }

  // Boundary analysis - very large document ID
  @Test
  void testGetDocumentSummary_LargeId() {
    Long documentId = Long.MAX_VALUE;
    Document document = new Document();
    document.setId(documentId);
    document.setSummary("Summary for large ID");

    when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

    String result = summarizationService.getDocumentSummary(documentId);

    assertEquals("Summary for large ID", result);
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

  // Boundary analysis - single character input
  @Test
  void testGenerateAiSummary_SingleCharacter() {
    String inputText = "a";

    String result = summarizationService.generateAiSummary(inputText);

    assertNotNull(result);
    assertEquals("a", result); // Should fallback to simple summary
  }

  // Valid equivalence partition - very long input
  @Test
  void testGenerateAiSummary_VeryLongInput() {
    String longText = "This is a very long document. ".repeat(1000);

    String result = summarizationService.generateAiSummary(longText);

    assertNotNull(result);
    assertTrue(result.length() <= 203); // Should fallback and be truncated
  }

  // Invalid equivalence partition - empty input
  @Test
  void testGenerateAiSummary_EmptyInput() {
    String result = summarizationService.generateAiSummary("");

    assertEquals("", result);
  }

  // Invalid equivalence partition - null input
  @Test
  void testGenerateAiSummary_NullInput() {
    assertThrows(Exception.class, () -> {
      summarizationService.generateAiSummary(null);
    });
  }

}