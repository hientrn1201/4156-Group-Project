package dev.coms4156.project.service;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import dev.coms4156.project.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractionService textExtractionService;
    private final DocumentChunkingService chunkingService;
    private final SimpleEmbeddingService embeddingService;

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
     * Process uploaded document through the complete pipeline: text extraction,
     * chunking, and embedding
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

            System.out.println("Successfully processed document: " + document.getFilename() +
                    " with " + chunks.size() + " chunks");
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
     * Process document with custom chunking parameters
     */
    @Transactional
    public Document processDocument(MultipartFile file, Integer chunkSize, Integer overlapSize) throws IOException {
        System.out.println("Starting document processing with custom chunking for: " + file.getOriginalFilename());

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

            // Step 2: Chunk the document with custom parameters (in memory only)
            System.out.println("Step 2: Chunking document with custom parameters: " + document.getId());
            List<DocumentChunk> chunks = chunkingService.chunkDocument(document, chunkSize, overlapSize);

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

            // Step 4: Generate summary
            System.out.println("Step 4: Generating summary for document: " + document.getId());
            String summary = generateSummary(extractedText);

            document.setSummary(summary);
            document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            document = documentRepository.save(document);

            System.out.println("Successfully processed document: " + document.getFilename() +
                    " with " + chunks.size() + " chunks");
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
     * Get document by ID
     */
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    /**
     * Get all documents
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Get documents by processing status
     */
    public List<Document> getDocumentsByStatus(Document.ProcessingStatus status) {
        return documentRepository.findByProcessingStatus(status);
    }

    /**
     * Delete document and all associated data
     */
    @Transactional
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
        System.out.println("Deleted document: " + id);
    }

    /**
     * Get chunks for a specific document
     */
    public List<DocumentChunk> getDocumentChunks(Long documentId) {
        return chunkingService.getChunksForDocument(documentId);
    }

    /**
     * Get chunk statistics for a document
     */
    public DocumentChunkingService.ChunkStatistics getChunkStatistics(Long documentId) {
        Optional<Document> document = getDocumentById(documentId);
        if (document.isEmpty()) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }
        return chunkingService.getChunkStatistics(document.get());
    }

    /**
     * Generate embeddings for chunks that don't have them yet
     */
    @Transactional
    public int generateEmbeddingsForDocument(Long documentId) {
        return embeddingService.generateEmbeddingsForDocument(documentId);
    }

    /**
     * Find similar chunks based on a query text
     */
    public List<DocumentChunk> findSimilarChunks(String queryText, int limit) {
        return embeddingService.findSimilarChunks(queryText, limit);
    }

    /**
     * Simple summary generation (placeholder implementation)
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

    /**
     * Reprocess a document (useful for updating embeddings or chunking parameters)
     */
    @Transactional
    public Document reprocessDocument(Long documentId, Integer chunkSize, Integer overlapSize) {
        Optional<Document> documentOpt = getDocumentById(documentId);
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }

        Document document = documentOpt.get();

        if (document.getExtractedText() == null || document.getExtractedText().trim().isEmpty()) {
            throw new IllegalArgumentException("Document has no extracted text to reprocess");
        }

        try {
            // Delete existing chunks
            chunkingService.deleteChunksForDocument(document);

            // Re-chunk with new parameters
            List<DocumentChunk> chunks = chunkingService.chunkDocument(document, chunkSize, overlapSize);

            // Generate new embeddings
            embeddingService.generateEmbeddings(chunks);

            document.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            document = documentRepository.save(document);

            System.out.println("Successfully reprocessed document: " + document.getFilename() +
                    " with " + chunks.size() + " chunks");
            return document;

        } catch (Exception e) {
            System.err.println("Error reprocessing document " + documentId + ": " + e.getMessage());
            e.printStackTrace();
            document.setProcessingStatus(Document.ProcessingStatus.FAILED);
            documentRepository.save(document);
            throw new RuntimeException("Document reprocessing failed", e);
        }
    }
}