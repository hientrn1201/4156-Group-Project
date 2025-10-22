package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentTextExtractionServiceTest {

  @Mock
  private MultipartFile multipartFile;

  private DocumentTextExtractionService textExtractionService;

  @BeforeEach
  void setUp() {
    textExtractionService = new DocumentTextExtractionService();
  }

  @Test
  void testExtractText_EmptyFile() throws IOException {
    // Given
    when(multipartFile.isEmpty()).thenReturn(true);

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      textExtractionService.extractText(multipartFile);
    });
  }

  @Test
  void testExtractText() throws IOException, TikaException {
    // Given
    String testContent = "This is a test document content.";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // When
    String result = textExtractionService.extractText(multipartFile);

    // Then
    assertEquals("This is a test document content.", result);
  }

  @Test
  void testDetectContentType() throws IOException {
    // Given
    String testContent = "Sample PDF content";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // When
    String result = textExtractionService.detectContentType(multipartFile);

    // Then
    assertTrue(result.startsWith("text/"));
  }

  @Test
  void testIsSupportedContentType() {
    // Test various supported content types
    assertTrue(textExtractionService.isSupportedContentType("text/plain"));
    assertTrue(textExtractionService.isSupportedContentType("application/pdf"));
    assertTrue(textExtractionService.isSupportedContentType("application/msword"));
    assertTrue(textExtractionService.isSupportedContentType(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    assertTrue(textExtractionService.isSupportedContentType("application/vnd.ms-excel"));
    assertTrue(textExtractionService.isSupportedContentType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    assertTrue(textExtractionService.isSupportedContentType("application/vnd.ms-powerpoint"));
    assertTrue(textExtractionService.isSupportedContentType(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
    assertTrue(textExtractionService.isSupportedContentType("application/rtf"));
    assertTrue(textExtractionService.isSupportedContentType("text/html"));
    assertTrue(textExtractionService.isSupportedContentType("text/xml"));
    assertTrue(textExtractionService.isSupportedContentType("application/xml"));
  }


}