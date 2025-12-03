package dev.coms4156.project.service;

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for extracting text content from various document formats using
 * Apache Tika.
 */
@Service
public class DocumentTextExtractionService {

  private final Tika tika;

  public DocumentTextExtractionService() {
    this.tika = new Tika();
  }

  /**
   * Extract text content from a multipart file.
   *
   * @param file The uploaded file
   * @return Extracted text content
   * @throws IOException   if file reading fails
   * @throws TikaException if text extraction fails
   */
  public String extractText(MultipartFile file) throws IOException, TikaException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }

    try (InputStream inputStream = file.getInputStream()) {
      // Extract text content
      String extractedText = tika.parseToString(inputStream);

      // Clean up the text (remove excessive whitespace, normalize line breaks)
      return cleanExtractedText(extractedText);
    }
  }

  /**
   * Extract text content from an input stream.
   *
   * @param inputStream The input stream containing the document
   * @return Extracted text content
   * @throws IOException   if stream reading fails
   * @throws TikaException if text extraction fails
   */
  public String extractText(InputStream inputStream) throws IOException, TikaException {
    if (inputStream == null) {
      throw new IllegalArgumentException("Input stream cannot be null");
    }

    String extractedText = tika.parseToString(inputStream);
    return cleanExtractedText(extractedText);
  }

  /**
   * Detect the content type of a file.
   *
   * @param file The uploaded file
   * @return Detected content type
   * @throws IOException if file reading fails
   */
  public String detectContentType(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }

    try (InputStream inputStream = file.getInputStream()) {
      return tika.detect(inputStream, file.getOriginalFilename());
    }
  }

  /**
   * Clean and normalize extracted text.
   *
   * @param rawText The raw extracted text
   * @return Cleaned text
   */
  private String cleanExtractedText(String rawText) {
    if (rawText == null || rawText.isBlank()) {
      return "";
    }

    // Remove excessive whitespace and normalize line breaks
    return rawText
        .replaceAll("\\r\\n", "\n") // Normalize Windows line endings
        .replaceAll("\\r", "\n") // Normalize Mac line endings
        .replaceAll("\\n{3,}", "\n\n") // Replace multiple newlines with double newlines
        .replaceAll("[ \\t]+", " ") // Replace multiple spaces/tabs with single space
        .trim();
  }

  /**
   * Check if the file type is supported for text extraction.
   *
   * @param contentType The content type of the file
   * @return true if supported, false otherwise
   */
  public boolean isSupportedContentType(String contentType) {
    if (contentType == null) {
      return false;
    }

    // Common supported document types
    return contentType.startsWith("text/") || "application/pdf".equals(contentType)
        || "application/msword".equals(contentType)
        || "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            .equals(contentType)
        || "application/vnd.ms-excel".equals(contentType)
        || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            .equals(contentType)
        || "application/vnd.ms-powerpoint".equals(contentType)
        || "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            .equals(contentType)
        || "application/rtf".equals(contentType)
        || "text/plain".equals(contentType)
        || "text/html".equals(contentType)
        || "text/xml".equals(contentType)
        || "application/xml".equals(contentType);
  }
}
