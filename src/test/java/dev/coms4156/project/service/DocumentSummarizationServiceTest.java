package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

}