package dev.coms4156.project.service;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.repository.DocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for generating document summarization.
 */
@Service
public class DocumentSummarizationService {

  private final DocumentRepository documentRepository;
  private final ChatClient chatClient;

  public DocumentSummarizationService(DocumentRepository documentRepository,
                                      ChatClient chatClient) {
    this.documentRepository = documentRepository;
    this.chatClient = chatClient;
  }

  /**
   * Generate a simple summary for a document.
   */
  @Transactional
  public String generateSummary(Document document) {
    System.out.println("Generating summary for document: " + document.getFilename());

    String text = document.getExtractedText();
    if (text == null || text.trim().isEmpty()) {
      System.err.println("No text content available for summarization: " + document.getId());
      return null;
    }

    try {
      String summary = generateAISummary(text);

      // Update document with summary
      document.setSummary(summary);
      documentRepository.save(document);

      System.out.println("Successfully generated summary for document: " + document.getFilename());
      return summary;

    } catch (Exception e) {
      System.err.println(
          "Error generating summary for document " + document.getId() + ": " + e.getMessage());
      throw new RuntimeException("Failed to generate summary", e);
    }
  }

  /**
   * Generate an AI-powered summary using Ollama.
   */
  public String generateAiSummary(String text) {
    System.out.println("Generating AI summary from text of length: " + text.length());

    try {
      // Use Ollama to generate a real AI summary
      String summary = chatClient.prompt()
          .user("Please provide a concise summary of this document (maximum 200 words): " + text)
          .call()
          .content();

      System.out.println("Generated AI summary of length: " + summary.length());
      return summary;

    } catch (Exception e) {
      System.err.println("Error generating AI summary: " + e.getMessage());
      // Fallback to simple summary if AI fails
      return generateSimpleSummary(text);
    }
  }

  /**
   * Generate a simple summary by extracting the first few sentences (fallback).
   */
  public String generateSimpleSummary(String text) {
    System.out.println("Generating simple summary from text of length: " + text.length());

    // Simple summarization: take first 200 characters
    if (text.length() <= 200) {
      return text;
    }

    String summary = text.substring(0, 200) + "...";
    System.out.println("Generated summary of length: " + summary.length());
    return summary;
  }

  /**
   * Get summary for a document.
   */
  public String getDocumentSummary(Long documentId) {
    return documentRepository.findById(documentId)
        .map(Document::getSummary)
        .orElse(null);
  }
}