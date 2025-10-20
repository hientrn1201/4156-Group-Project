package dev.coms4156.project.service;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for managing document processing and CRUD operations.
 * <p>
 * The {@code DocumentService} handles the complete document lifecycle, including upload,
 * text extraction, chunking, embedding generation, and summarization. It integrates with
 * {@link DocumentTextExtractionService}, {@link DocumentChunkingService}, and
 * {@link SimpleEmbeddingService} to orchestrate end-to-end document processing.
 * </p>
 */
@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final DocumentTextExtractionService textExtractionService;
  private final DocumentChunkingService chunkingService;
  private final SimpleEmbeddingService embeddingService;

  /**
   * Constructs a new {@code DocumentService} instance with all required dependencies.
   *
   * @param documentRepository    the {@link DocumentRepository} used for persisting and retrieving
   *                              documents.
   * @param textExtractionService the {@link DocumentTextExtractionService} used to extract raw
   *                              text from uploaded files.
   * @param chunkingService       the {@link DocumentChunkingService} responsible for dividing text
   *                              into manageable chunks.
   * @param embeddingService      the {@link SimpleEmbeddingService} responsible for generating
   *                              vector embeddings for each chunk.
   */
  public DocumentService(DocumentRepository documentRepository,
                         DocumentTextExtractionService textExtractionService,
                         DocumentChunkingService chunkingService,
                         SimpleEmbeddingService embeddingService) {
    this.documentRepository = documentRepository;
    this.textExtractionService = textExtractionService;
    this.chunkingService = chunkingService;
    this.embeddingService = embeddingService;
  }

  /**
   * Processes an uploaded document through the complete pipeline.
   *
   * @param file the uploaded {@link MultipartFile} to process.
   * @return the processed {@link Document} entity, including extracted text and summary.
   * @throws IOException              if reading the uploaded file fails.
   * @throws IllegalArgumentException if the file is empty or of an unsupported type.
   * @throws RuntimeException         if any pipeline step fails.
   */
  @Transactional
  public Document processDocument(MultipartFile file) throws IOException {
    System.out.println("Starting document processing for: " + file.getOriginalFilename());

    // Validate file
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    // Check if file type is supported
    String contentType = textExtractionService.detectContentType(file);
    if (!textExtractionService.isSupportedContentType(contentType)) {
      throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

    // Create document entity
    Document document = new Document();
    document.setFilename(file.getOriginalFilename());
    document.setFileSize(file.getSize());
    document.setContentType(contentType);
    document.setProcessingStatus(Document.ProcessingStatus.UPLOADED);

    // Save document
    document = documentRepository.save(document);

    try {
      // Step 1: Extract text using Apache Tika
      System.out.println("Step 1: Extracting text from document: " + document.getId());
      String extractedText = textExtractionService.extractText(file);

      if (extractedText == null || extractedText.trim().isEmpty()) {
        throw new RuntimeException("No text could be extracted from the document");
      }

      document.setExtractedText(extractedText);
      document.setProcessingStatus(Document.ProcessingStatus.TEXT_EXTRACTED);
      document = documentRepository.save(document);

      // Step 2: Chunk the document (in memory only)
      System.out.println("Step 2: Chunking document: " + document.getId());
      List<DocumentChunk> chunks = chunkingService.chunkDocument(document);

      if (chunks.isEmpty()) {
        throw new RuntimeException("No chunks could be created from the document");
      }

      // Step 3: Generate embeddings for chunks
      System.out.println("Step 3: Generating embeddings for " + chunks.size() + " chunks");
      embeddingService.generateEmbeddings(chunks);

      document.setProcessingStatus(Document.ProcessingStatus.CHUNKED);
      document = documentRepository.save(document);

      document.setProcessingStatus(Document.ProcessingStatus.EMBEDDINGS_GENERATED);
      document = documentRepository.save(document);

      // Step 4: Generate summary (optional - can be implemented later)
      System.out.println("Step 4: Generating summary for document: " + document.getId());
      String summary = generateSummary(extractedText);

      document.setSummary(summary);
      document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
      document = documentRepository.save(document);

      System.out.println("Successfully processed document: " + document.getFilename() + " with "
          + chunks.size() + " chunks");
      return document;

    } catch (Exception e) {
      System.err.println("Error processing document " + document.getId() + ": " + e.getMessage());
      e.printStackTrace();
      document.setProcessingStatus(Document.ProcessingStatus.FAILED);
      documentRepository.save(document);
      throw new RuntimeException("Document processing failed", e);
    }
  }


  /**
   * Retrieves a document by its unique ID.
   *
   * @param id the document ID.
   * @return an {@link Optional} containing the document if found, or empty if not found.
   */
  public Optional<Document> getDocumentById(Long id) {
    return documentRepository.findById(id);
  }

  /**
   * Retrieves all stored documents.
   *
   * @return a list of all {@link Document} entities.
   */
  public List<Document> getAllDocuments() {
    return documentRepository.findAll();
  }

  /**
   * Retrieves all documents that match a specific processing status.
   *
   * @param status the {@link Document.ProcessingStatus} filter.
   * @return a list of documents with the specified status.
   */
  public List<Document> getDocumentsByStatus(Document.ProcessingStatus status) {
    return documentRepository.findByProcessingStatus(status);
  }

  /**
   * Deletes a document and all associated data such as chunks and embeddings.
   *
   * @param id the ID of the document to delete.
   */
  @Transactional
  public void deleteDocument(Long id) {
    documentRepository.deleteById(id);
    System.out.println("Deleted document: " + id);
  }

  /**
   * Retrieves all {@link DocumentChunk} instances belonging to a given document.
   *
   * @param documentId the ID of the document.
   * @return a list of chunks for the specified document.
   * @throws IllegalArgumentException if the document does not exist.
   */
  public List<DocumentChunk> getDocumentChunks(Long documentId) {
    return chunkingService.getChunksForDocument(documentId);
  }

  /**
   * Retrieves statistical information about the chunking of a document,
   * including the number of chunks, average chunk length, and overlap ratio.
   *
   * @param documentId the ID of the document to analyze.
   * @return a {@link DocumentChunkingService.ChunkStatistics} object containing metrics.
   * @throws IllegalArgumentException if the document is not found.
   */
  public DocumentChunkingService.ChunkStatistics getChunkStatistics(Long documentId) {
    Optional<Document> document = getDocumentById(documentId);
    if (document.isEmpty()) {
      throw new IllegalArgumentException("Document not found: " + documentId);
    }
    return chunkingService.getChunkStatistics(document.get());
  }

  /**
   * Generates embeddings for all chunks of a given document that currently
   * lack embeddings in the database.
   *
   * @param documentId the ID of the document to process.
   * @return the number of chunks for which embeddings were generated.
   */
  @Transactional
  public int generateEmbeddingsForDocument(Long documentId) {
    return embeddingService.generateEmbeddingsForDocument(documentId);
  }

  /**
   * Finds chunks that are semantically similar to a given query text.
   *
   * @param queryText the query string to match against.
   * @param limit     the maximum number of similar chunks to return.
   * @return a list of the most similar {@link DocumentChunk} entities.
   */
  public List<DocumentChunk> findSimilarChunks(String queryText, int limit) {
    return embeddingService.findSimilarChunks(queryText, limit);
  }


  /**
   * Generates a simple summary of the document’s text content (placeholder for now).
   *
   * @param text the extracted text to summarize.
   * @return a short summary string, or a placeholder message if text is unavailable.
   */
  private String generateSummary(String text) {
    if (text == null || text.trim().isEmpty()) {
      return "No summary available";
    }

    // Simple truncation-based summary (first 200 characters)
    String cleanedText = text.replaceAll("\\s+", " ").trim();
    if (cleanedText.length() <= 200) {
      return cleanedText;
    }

    // Try to break at sentence boundary
    String truncated = cleanedText.substring(0, 200);
    int lastPeriod = truncated.lastIndexOf('.');
    if (lastPeriod > 100) {
      return truncated.substring(0, lastPeriod + 1);
    }

    return truncated + "...";
  }
}