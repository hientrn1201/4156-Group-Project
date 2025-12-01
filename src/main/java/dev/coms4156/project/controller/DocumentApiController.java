package dev.coms4156.project.controller;

import dev.coms4156.project.dtos.DocumentChunkDTO;
import dev.coms4156.project.dtos.DocumentDTO;
import dev.coms4156.project.dtos.DocumentListResponse;
import dev.coms4156.project.dtos.DocumentRelationshipInfoResponse;
import dev.coms4156.project.dtos.DocumentRelationshipDTO;
import dev.coms4156.project.dtos.DocumentSearchResponse;
import dev.coms4156.project.dtos.DocumentStatsResponse;
import dev.coms4156.project.dtos.DocumentStatusCounts;
import dev.coms4156.project.dtos.DocumentSummaryResponse;
import dev.coms4156.project.dtos.DocumentUploadResponse;
import dev.coms4156.project.dtos.ErrorResponse;
import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.service.ApiLoggingService;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import dev.coms4156.project.service.RagService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final Logger logger = LoggerFactory.getLogger(DocumentApiController.class);

  private final DocumentService documentService;
  private final DocumentSummarizationService summarizationService;
  private final RagService ragService;

  @Autowired
  private ApiLoggingService apiLoggingService;

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
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentUploadResponse.class))),
      @ApiResponse(responseCode = "400",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
  })
  public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
    String requestId = apiLoggingService.generateRequestId();
    String clientId = apiLoggingService.getClientId(
        request.getHeader("X-Client-ID"),
        request.getRemoteAddr());

    try {
      logger.info("Received file upload: {} from client: {} (requestId: {})",
          file.getOriginalFilename(), clientId, requestId);

      Document document = documentService.processDocument(file);

      // Skip RAG vector store ingestion - use existing document_chunks table instead
      // The document_chunks table already contains the embeddings for RAG operations

      DocumentUploadResponse response = new DocumentUploadResponse(
          document.getId(),
          document.getFilename(),
          document.getProcessingStatus(),
          "Document uploaded and processed successfully");

      logger.info("Successfully processed document: {} for client: {}",
          document.getFilename(), clientId);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      logger.error("Invalid file upload from client: {} - {}", clientId, e.getMessage());
      ErrorResponse error = new ErrorResponse(e.getMessage());

      return ResponseEntity.badRequest().body(error);

    } catch (IOException e) {
      logger.error("File upload error from client: {} - {}", clientId, e.getMessage());

      ErrorResponse error = new ErrorResponse("File upload failed");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

    } catch (Exception e) {
      logger.error("Document processing error from client: {} - {}", clientId, e.getMessage(), e);

      ErrorResponse error = new ErrorResponse("Document processing failed");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents/{id}.
   * Retrieve document metadata, summaries, and processing status.
   */
  @GetMapping("/documents/{id}")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentDTO.class))),
      @ApiResponse(responseCode = "404"),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> getDocument(@PathVariable Long id) {
    try {
      Optional<Document> document = documentService.getDocumentById(id);

      if (document.isPresent()) {
        Document doc = document.get();

        DocumentDTO response = DocumentDTO.fromDocument(doc);

        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.notFound().build();
      }

    } catch (Exception e) {
      System.err.println("Error retrieving document " + id + ": " + e.getMessage());

      ErrorResponse error = new ErrorResponse("Failed to retrieve document");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/relationships/{documentId}.
   * Retrieve related documents (knowledge graph edges).
   */
  @GetMapping("/relationships/{documentId}")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(
              schema = @Schema(implementation = DocumentRelationshipInfoResponse.class)
          )
      ),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> getDocumentRelationships(@PathVariable Long documentId) {
    try {
      // For now, return empty relationships - this would be implemented with actual
      // relationship analysis
      List<DocumentRelationshipDTO> relationships = documentService
          .getRelationshipsForDocument(documentId).stream()
          .map(DocumentRelationshipDTO::fromDocumentRelationship)
          .toList();

      DocumentRelationshipInfoResponse response = new DocumentRelationshipInfoResponse(
          documentId,
          relationships,
          relationships.size(),
          "Successfully retrieved document relationships");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      System.err.println("Error getting document relationships: " + e.getMessage());

      ErrorResponse error = new ErrorResponse("Failed to get document relationships");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents/{id}/summary.
   * Retrieve generated summary.
   */
  @GetMapping("/documents/{id}/summary")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentSummaryResponse.class))),
      @ApiResponse(responseCode = "404"),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> getDocumentSummary(@PathVariable Long id) {
    try {
      String summary = summarizationService.getDocumentSummary(id);

      if (summary != null) {
        DocumentSummaryResponse response = new DocumentSummaryResponse(
            id,
            summary);

        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.notFound().build();
      }

    } catch (Exception e) {
      System.err.println("Error retrieving summary for document " + id + ": " + e.getMessage());

      ErrorResponse error = new ErrorResponse("Failed to retrieve summary");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/search/{text}.
   * Retrieve top 3 relevant documents based on text input.
   */
  @GetMapping("/search/{text}")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentSearchResponse.class))),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> searchDocuments(@PathVariable String text,
                                           HttpServletRequest request) {
    String requestId = apiLoggingService.generateRequestId();
    String clientId = apiLoggingService.getClientId(
        request.getHeader("X-Client-ID"),
        request.getRemoteAddr());

    try {
      logger.info("Performing search for: '{}' from client: {} (requestId: {})", text, clientId,
          requestId);

      // Use the document service to find similar chunks
      List<DocumentChunk> similarChunks = documentService.findSimilarChunks(text, 3);

      DocumentSearchResponse response = new DocumentSearchResponse(
          text,
          similarChunks.stream().map(DocumentChunkDTO::fromDocumentChunk).toList(),
          similarChunks.size(),
          "Search completed successfully");

      logger.info("Search completed for client: {} - found {} results", clientId,
          similarChunks.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Search error from client: {} - {}", clientId, e.getMessage(), e);

      ErrorResponse error = new ErrorResponse("Search failed: " + e.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * GET /api/v1/documents.
   * Retrieve documents.
   *
   * @param filename Optional filename to match on.
   * @return ResponseEntity containing list of all documents
   */
  @GetMapping("/documents")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentListResponse.class))),
      @ApiResponse(responseCode = "500",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<?> getAllDocuments(
      @RequestParam(required = false) String filename) {

    try {
      List<Document> documents;
      if (filename == null || filename.trim().isEmpty()) {
        documents = documentService.getAllDocuments();
      } else {
        documents = documentService.getDocumentsByFilename(filename);
      }

      List<DocumentDTO> docsDto = documents.stream()
          .map(DocumentDTO::fromDocument)
          .toList();

      DocumentListResponse response = new DocumentListResponse(
          docsDto,
          (long) documents.size(),
          "Documents retrieved successfully");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      System.err.println("Error retrieving documents: " + e.getMessage());
      ErrorResponse error = new ErrorResponse("Failed to retrieve documents");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
  @ApiResponses({
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentListResponse.class))),
  })
  public ResponseEntity<?> getDocumentsWithSummaries() {
    List<Document> all = documentService.getAllDocuments();
    List<Document> documents = new ArrayList<>();

    for (Document d : all) {
      String s = d.getSummary();
      if (s != null && !s.isBlank()) {
        documents.add(d);
      }
    }

    DocumentListResponse response = new DocumentListResponse(
        documents.stream().map(DocumentDTO::fromDocument).toList(),
        (long) documents.size(),
        "Documents with summaries retrieved successfully");

    return ResponseEntity.ok(response);
  }

  /**
   * GET /api/v1/documents/stats.
   * Get docs with their statistics.
   */
  @GetMapping("/documents/stats")
  @ApiResponses({
      @ApiResponse(responseCode = "200",
          content = @Content(schema = @Schema(implementation = DocumentStatsResponse.class)))
  })
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
    DocumentStatusCounts statusCounts = new DocumentStatusCounts(
        uploaded,
        textExtracted,
        chunked,
        embeddingsGenerated,
        summarized,
        completed,
        failed);

    double completionRate;
    double failureRate;

    if (total > 0) {
      completionRate = (double) completed / (double) total;
      failureRate = (double) failed / (double) total;
    } else {
      completionRate = 0.0;
      failureRate = 0.0;
    }

    DocumentStatsResponse response = new DocumentStatsResponse(
        total,
        statusCounts,
        completionRate,
        failureRate);

    return ResponseEntity.ok(response);
  }

}
