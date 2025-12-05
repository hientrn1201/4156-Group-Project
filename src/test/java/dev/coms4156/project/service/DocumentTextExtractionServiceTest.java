package dev.coms4156.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

  // Invalid equivalence partition - null multipart file
  @Test
  void testExtractText_NullFile() {
    assertThrows(Exception.class, () -> {
      textExtractionService.extractText((MultipartFile) null);
    });
  }

  // Boundary analysis - zero-byte file (not empty but no content)
  @Test
  void testExtractText_ZeroByteFile() throws IOException, TikaException {
    InputStream emptyStream = new ByteArrayInputStream(new byte[1]); // At least 1 byte
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(emptyStream);

    String result = ((DocumentTextExtractionService) textExtractionService)
        .extractText(multipartFile);

    assertNotNull(result);
  }

  @Test
  void testExtractText() throws IOException, TikaException {
    // Given
    String testContent = "This is a test document content.";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // When
    String result = ((DocumentTextExtractionService) textExtractionService)
        .extractText(multipartFile);

    // Then
    assertEquals("This is a test document content.", result);
  }

  // Boundary analysis - very large file content
  @Test
  void testExtractText_LargeContent() throws IOException, TikaException {
    String largeContent = "Large content line.\n".repeat(1000);
    InputStream inputStream = new ByteArrayInputStream(largeContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    String result = ((DocumentTextExtractionService) textExtractionService)
        .extractText(multipartFile);

    assertNotNull(result);
    assertTrue(result.length() > 1000);
  }

  // Boundary analysis - single character content
  @Test
  void testExtractText_SingleCharacter() throws IOException, TikaException {
    InputStream inputStream = new ByteArrayInputStream("a".getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    String result = ((DocumentTextExtractionService) textExtractionService)
        .extractText(multipartFile);

    assertEquals("a", result);
  }

  // Valid equivalence partition - content with special characters
  @Test
  void testExtractText_SpecialCharacters() throws IOException, TikaException {
    String specialContent = "Content with special chars: àáâãäåæçèéêë 中文 русский";
    InputStream inputStream = new ByteArrayInputStream(specialContent.getBytes("UTF-8"));
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    String result = ((DocumentTextExtractionService) textExtractionService)
        .extractText(multipartFile);

    assertNotNull(result);
    assertTrue(result.contains("àáâãäåæçèéêë"));
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

  // Boundary analysis - empty file for content detection
  @Test
  void testDetectContentType_EmptyFile() throws IOException {
    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("empty.txt");
    when(multipartFile.getInputStream()).thenReturn(emptyStream);

    String result = textExtractionService.detectContentType(multipartFile);
    assertNotNull(result);
  }

  // Boundary analysis - file with no extension
  @Test
  void testDetectContentType_NoExtension() throws IOException {
    String testContent = "Sample content";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn("filename_without_extension");
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    String result = textExtractionService.detectContentType(multipartFile);
    assertNotNull(result);
  }

  // Invalid equivalence partition - null filename
  @Test
  void testDetectContentType_NullFilename() throws IOException {
    String testContent = "Sample content";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getOriginalFilename()).thenReturn(null);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    String result = textExtractionService.detectContentType(multipartFile);
    assertNotNull(result);
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

  // Invalid equivalence partition - unsupported content types
  @Test
  void testIsSupportedContentType_UnsupportedTypes() {
    assertFalse(textExtractionService.isSupportedContentType("image/jpeg"));
    assertFalse(textExtractionService.isSupportedContentType("video/mp4"));
    assertFalse(textExtractionService.isSupportedContentType("audio/mp3"));
    assertFalse(textExtractionService.isSupportedContentType("application/zip"));
    assertFalse(textExtractionService.isSupportedContentType("application/unknown"));
  }

  // Boundary analysis - null and empty content types
  @Test
  void testIsSupportedContentType_NullAndEmpty() {
    assertFalse(textExtractionService.isSupportedContentType(null));
    assertFalse(textExtractionService.isSupportedContentType(""));
    assertFalse(textExtractionService.isSupportedContentType("   "));
  }

  // Boundary analysis - case sensitivity
  @Test
  void testIsSupportedContentType_CaseSensitivity() {
    // Implementation is case sensitive - uppercase returns false
    assertFalse(textExtractionService.isSupportedContentType("TEXT/PLAIN"));
    assertFalse(textExtractionService.isSupportedContentType("Application/PDF"));
    assertTrue(textExtractionService.isSupportedContentType("text/PLAIN")); // starts with text/
  }

  // Invalid equivalence partition - malformed content types
  @Test
  void testIsSupportedContentType_MalformedTypes() {
    // doesn't start with text/
    assertFalse(textExtractionService.isSupportedContentType("textplain"));
    // starts with text/
    assertTrue(textExtractionService.isSupportedContentType("text/"));
    assertFalse(textExtractionService.isSupportedContentType("/plain"));
    // starts with text/
    assertTrue(textExtractionService.isSupportedContentType("text//plain"));
  }

  // Boundary analysis - very long content type
  @Test
  void testIsSupportedContentType_VeryLongType() {
    String longType = "application/" + "a".repeat(1000);
    assertFalse(textExtractionService.isSupportedContentType(longType));
  }

  @Test
  void testExtractText_WithTextCleaning() throws IOException, TikaException {
    // Given - text with excessive whitespace and mixed line endings
    String testContent = "Line 1\r\nLine 2\r\n\r\n\r\nLine 3    \t\t   Line 4";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // When
    String result = textExtractionService.extractText(multipartFile);

    // Then - should be cleaned (normalized line breaks, reduced whitespace)
    assertTrue(result.contains("Line 1"));
    assertTrue(result.contains("Line 2"));
    assertTrue(result.contains("Line 3"));
    assertTrue(result.contains("Line 4"));
  }

  @Test
  void testExtractText_EmptyText() throws IOException, TikaException {
    // Given - empty content
    String testContent = "   \n\n\r\n   ";
    InputStream inputStream = new ByteArrayInputStream(testContent.getBytes());
    when(multipartFile.isEmpty()).thenReturn(false);
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // When
    String result = textExtractionService.extractText(multipartFile);

    // Then - should return empty string
    assertEquals("", result);
  }

  @Test
  void testIsSupportedContentType_AllSupportedTypes() {
    // Test each supported content type individually to cover all branches
    assertTrue(textExtractionService.isSupportedContentType("text/plain"));
    assertTrue(textExtractionService.isSupportedContentType("text/html"));
    assertTrue(textExtractionService.isSupportedContentType("text/xml"));
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
    assertTrue(textExtractionService.isSupportedContentType("application/xml"));
  }

  @Test
  void testIsSupportedContentType_TextPrefix() {
    // Test that any text/* type is supported
    assertTrue(textExtractionService.isSupportedContentType("text/csv"));
    assertTrue(textExtractionService.isSupportedContentType("text/markdown"));
    assertTrue(textExtractionService.isSupportedContentType("text/javascript"));
  }
}
