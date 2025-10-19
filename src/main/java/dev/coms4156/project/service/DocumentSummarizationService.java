package dev.coms4156.project.service;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentSummarizationService {

    private final DocumentRepository documentRepository;

    public DocumentSummarizationService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Generate a simple summary for a document
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
            String summary = generateSimpleSummary(text);

            // Update document with summary
            document.setSummary(summary);
            documentRepository.save(document);

            System.out.println("Successfully generated summary for document: " + document.getFilename());
            return summary;

        } catch (Exception e) {
            System.err.println("Error generating summary for document " + document.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to generate summary", e);
        }
    }

    /**
     * Generate a simple summary by extracting the first few sentences
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
     * Get summary for a document
     */
    public String getDocumentSummary(Long documentId) {
        return documentRepository.findById(documentId)
                .map(Document::getSummary)
                .orElse(null);
    }
}