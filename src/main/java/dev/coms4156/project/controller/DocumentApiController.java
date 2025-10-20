package dev.coms4156.project.controller;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import dev.coms4156.project.service.RagService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for document management and RAG operations.
 */
@RestController
@RequestMapping("/api/v1")
public class DocumentApiController {

  private final DocumentService documentService;
  private final DocumentSummarizationService summarizationService;
  private final RagService ragService;

  /**
   * Constructor for DocumentApiController.
   *
   * @param documentService      The document service
   * @param summarizationService The summarization service
   * @param ragService           The RAG service
   */
  public DocumentApiController(DocumentService documentService,
                               DocumentSummarizationService summarizationService,
                               RagService ragService) {
    this.documentService = documentService;
    this.summarizationService = summarizationService;
    this.ragService = ragService;
  }

  /**
   * POST /api/v1/documents.
   * Upload a document for processing -- extract text, chunking and embedded.
   */
  @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
    try {
      System.out.println("Received file upload: " + file.getOriginalFilename());
      Document document = documentService.processDocument(file);

      // Skip RAG vector store ingestion - use existing document_chunks table instead
      // The document_chunks table already contains the embeddings for RAG operations

      Map<String, Object> response = new HashMap<>();
      response.put("documentId", document.getId());
      response.put("filename", document.getFilename());
      response.put("status", document.getProcessingStatus());
      response.put("message", "Document uploaded and processed successfully");

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      System.err.println("Invalid file upload: " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(error);

    } catch (IOException e) {
      System.err.println("File upload error: " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "File upload failed");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

    } catch (Exception e) {
      System.err.println("Document processing error: " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "Document processing failed");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents/{id}.
   * Retrieve document metadata, summaries, and processing status.
   */
  @GetMapping("/documents/{id}")
  public ResponseEntity<?> getDocument(@PathVariable Long id) {
    try {
      Optional<Document> document = documentService.getDocumentById(id);

      if (document.isPresent()) {
        Document doc = document.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", doc.getId());
        response.put("filename", doc.getFilename());
        response.put("contentType", doc.getContentType());
        response.put("fileSize", doc.getFileSize());
        response.put("processingStatus", doc.getProcessingStatus());
        response.put("summary", doc.getSummary());
        response.put("uploadedAt", doc.getUploadedAt());
        response.put("updatedAt", doc.getUpdatedAt());

        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.notFound().build();
      }

    } catch (Exception e) {
      System.err.println("Error retrieving document " + id + ": " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to retrieve document");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/relationships/{documentId}.
   * Retrieve related documents (knowledge graph edges).
   */
  @GetMapping("/relationships/{documentId}")
  public ResponseEntity<?> getDocumentRelationships(@PathVariable Long documentId) {
    try {
      // For now, return empty relationships - this would be implemented with actual
      // relationship analysis
      Map<String, Object> response = new HashMap<>();
      response.put("documentId", documentId);
      response.put("relationships", List.of());
      response.put("count", 0);
      response.put("message", "Relationship analysis not yet implemented");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      System.err.println("Error getting document relationships: " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to get document relationships");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents/{id}/summary.
   * Retrieve generated summary.
   */
  @GetMapping("/documents/{id}/summary")
  public ResponseEntity<?> getDocumentSummary(@PathVariable Long id) {
    try {
      String summary = summarizationService.getDocumentSummary(id);

      if (summary != null) {
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", id);
        response.put("summary", summary);
        return ResponseEntity.ok(response);
      } else {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Summary not found for document");
        return ResponseEntity.notFound().build();
      }

    } catch (Exception e) {
      System.err.println("Error retrieving summary for document " + id + ": " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to retrieve summary");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/search/{text}.
   * Retrieve top 3 relevant documents based on text input.
   */
  @GetMapping("/search/{text}")
  public ResponseEntity<?> searchDocuments(@PathVariable String text) {
    try {
      System.out.println("Performing search for: " + text);

      // Use the document service to find similar chunks
      List<DocumentChunk> similarChunks = documentService.findSimilarChunks(text, 3);

      Map<String, Object> response = new HashMap<>();
      response.put("query", text);
      response.put("results", similarChunks);
      response.put("count", similarChunks.size());
      response.put("message", "Search completed successfully");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      System.err.println("Search error: " + e.getMessage());
      Map<String, String> error = new HashMap<>();
      error.put("error", "Search failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents.
   * Retrieve all documents.
   *
   * @return ResponseEntity containing list of all documents
   */
  @GetMapping("/documents")
  public ResponseEntity<?> getAllDocuments() {

    try {

      List<Document> documents = documentService.getAllDocuments();
      Map<String, Object> response = new HashMap<>();
      response.put("documents", documents);
      response.put("count", documents.size());
      response.put("message", "Documents retrieved");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
    }
  }

  /**
   * DELETE /api/v1/documents/{id}.
   * Delete a document by ID.
   *
   * @param id The document ID to delete
   * @return ResponseEntity indicating success or failure
   */
  @DeleteMapping("/documents/{id}")
  public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
    if (documentService.getDocumentById(id).isEmpty()) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Document not found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    documentService.deleteDocument(id);

    return ResponseEntity.ok(Map.of("documentId", id, "message", "Document deleted successfully"));
  }

  /**
   * GET /api/v1/documents/summaries.
   * Get docs with summaries.
   */
  @GetMapping("/documents/summaries")
  public ResponseEntity<Map<String, Object>> getDocumentsWithSummaries() {
    List<Document> all = documentService.getAllDocuments();
    List<Document> documents = new ArrayList<>();

    for (Document d : all) {
      String s = d.getSummary();
      if (s != null && !s.isBlank()) {
        documents.add(d);
      }
    }

    Map<String, Object> body = new HashMap<>();
    body.put("documents", documents);
    body.put("count", documents.size());
    return ResponseEntity.ok(body);
  }

  /**
   * GET /api/v1/documents/stat.
   * Get docs with their statistics.
   */
  @GetMapping("/documents/stats")
  public ResponseEntity<?> getProcessingStatistics() {
    List<Document> allDocuments = documentService.getAllDocuments();
    final long total = allDocuments.size();

    long uploaded = 0;
    long textExtracted = 0;
    long chunked = 0;
    long embeddingsGenerated = 0;
    long summarized = 0;
    long completed = 0;
    long failed = 0;

    for (Document doc : allDocuments) {
      Document.ProcessingStatus status = doc.getProcessingStatus();
      if (status == Document.ProcessingStatus.UPLOADED) {
        uploaded++;
      } else if (status == Document.ProcessingStatus.TEXT_EXTRACTED) {
        textExtracted++;
      } else if (status == Document.ProcessingStatus.CHUNKED) {
        chunked++;
      } else if (status == Document.ProcessingStatus.EMBEDDINGS_GENERATED) {
        embeddingsGenerated++;
      } else if (status == Document.ProcessingStatus.SUMMARIZED) {
        summarized++;
      } else if (status == Document.ProcessingStatus.COMPLETED) {
        completed++;
      } else if (status == Document.ProcessingStatus.FAILED) {
        failed++;
      }
    }
    Map<String, Long> statusCounts = new HashMap<>();
    statusCounts.put("UPLOADED", uploaded);
    statusCounts.put("TEXT_EXTRACTED", textExtracted);
    statusCounts.put("CHUNKED", chunked);
    statusCounts.put("EMBEDDINGS_GENERATED", embeddingsGenerated);
    statusCounts.put("SUMMARIZED", summarized);
    statusCounts.put("COMPLETED", completed);
    statusCounts.put("FAILED", failed);

    double completionRate;
    double failureRate;

    if (total > 0) {
      completionRate = (double) completed / (double) total;
      failureRate = (double) failed / (double) total;
    } else {
      completionRate = 0.0;
      failureRate = 0.0;
    }
    return ResponseEntity.ok(
        Map.of("total", total, "byStatus", statusCounts, "completionRate", completionRate,
            "failureRate", failureRate));
  }

}