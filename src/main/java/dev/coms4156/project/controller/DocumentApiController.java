package dev.coms4156.project.controller;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.service.DocumentService;
import dev.coms4156.project.service.DocumentSummarizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class DocumentApiController {

    private final DocumentService documentService;
    private final DocumentSummarizationService summarizationService;

    public DocumentApiController(DocumentService documentService,
            DocumentSummarizationService summarizationService) {
        this.documentService = documentService;
        this.summarizationService = summarizationService;
    }

    /**
     * POST /api/v1/documents
     * Upload a document for processing -- extract text, chunking and embedded
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Received file upload: " + file.getOriginalFilename());
            Document document = documentService.processDocument(file);

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
     * GET /api/v1/documents/{id}
     * Retrieve document metadata, summaries, and processing status
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
     * GET /api/v1/relationships/{documentId}
     * Retrieve related documents (knowledge graph edges)
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
     * GET /api/v1/documents/{id}/summary
     * Retrieve generated summary
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
     * GET /api/v1/search/{text}
     * Retrieve top 3 relevant documents based on text input
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
}