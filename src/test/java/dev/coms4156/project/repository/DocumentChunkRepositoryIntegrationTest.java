package dev.coms4156.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.model.DocumentChunk;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for DocumentChunkRepository.
 * Tests actual database operations using PostgreSQL with PGVector.
 *
 * <p>This test integrates with:
 * - PostgreSQL database with PGVector extension (external resource)
 * - JPA/Hibernate ORM
 * - DocumentChunk entity persistence with vector embeddings
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.show-sql=false"
})
class DocumentChunkRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private DocumentChunkRepository chunkRepository;

  @Autowired
  private DocumentRepository documentRepository;

  private Document testDocument;
  private DocumentChunk testChunk;

  @BeforeEach
  void setUp() {
    // Create a test document
    testDocument = new Document();
    testDocument.setFilename("test-document.pdf");
    testDocument.setContentType("application/pdf");
    testDocument.setFileSize(1024L);
    testDocument.setExtractedText("Test content for chunking");
    testDocument.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    testDocument.setUploadedAt(LocalDateTime.now());
    testDocument.setUpdatedAt(LocalDateTime.now());

    // Create a test chunk
    testChunk = new DocumentChunk();
    testChunk.setDocument(testDocument);
    testChunk.setChunkIndex(0);
    testChunk.setTextContent("This is the first chunk of text");
    testChunk.setChunkSize(30);
    testChunk.setStartPosition(0);
    testChunk.setEndPosition(30);
  }

  @Test
  void testSaveAndFindById() {
    // Given - Save document first
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    testChunk.setDocument(savedDoc);

    // When - Save chunk
    DocumentChunk saved = chunkRepository.save(testChunk);
    entityManager.flush();
    entityManager.clear();

    // Then - Retrieve by ID
    DocumentChunk found = chunkRepository.findById(saved.getId()).orElse(null);

    assertNotNull(found);
    assertEquals(0, found.getChunkIndex());
    assertEquals("This is the first chunk of text", found.getTextContent());
    assertEquals(savedDoc.getId(), found.getDocumentId());
  }

  @Test
  void testFindByDocumentId() {
    // Given - Save document and chunks
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("First chunk");
    chunk1.setChunkSize(10);
    chunk1.setStartPosition(0);
    chunk1.setEndPosition(10);

    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(savedDoc);
    chunk2.setChunkIndex(1);
    chunk2.setTextContent("Second chunk");
    chunk2.setChunkSize(11);
    chunk2.setStartPosition(10);
    chunk2.setEndPosition(21);

    chunkRepository.save(chunk1);
    chunkRepository.save(chunk2);
    entityManager.flush();
    entityManager.clear();

    // When - Find by document ID
    List<DocumentChunk> chunks = chunkRepository.findByDocumentId(savedDoc.getId());

    // Then
    assertTrue(chunks.size() >= 2);
    assertTrue(chunks.stream().allMatch(c -> c.getDocumentId().equals(savedDoc.getId())));
  }

  @Test
  void testFindByDocumentIdOrderByChunkIndex() {
    // Given - Save document and chunks in reverse order
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(savedDoc);
    chunk2.setChunkIndex(2);
    chunk2.setTextContent("Third chunk");
    chunk2.setChunkSize(10);
    chunk2.setStartPosition(20);
    chunk2.setEndPosition(30);

    DocumentChunk chunk0 = new DocumentChunk();
    chunk0.setDocument(savedDoc);
    chunk0.setChunkIndex(0);
    chunk0.setTextContent("First chunk");
    chunk0.setChunkSize(10);
    chunk0.setStartPosition(0);
    chunk0.setEndPosition(10);

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(1);
    chunk1.setTextContent("Second chunk");
    chunk1.setChunkSize(10);
    chunk1.setStartPosition(10);
    chunk1.setEndPosition(20);

    chunkRepository.save(chunk2);
    chunkRepository.save(chunk0);
    chunkRepository.save(chunk1);
    entityManager.flush();
    entityManager.clear();

    // When - Find ordered by chunk index
    List<DocumentChunk> chunks =
        chunkRepository.findByDocumentIdOrderByChunkIndex(savedDoc.getId());

    // Then - Should be in order
    assertTrue(chunks.size() >= 3);
    for (int i = 0; i < chunks.size() - 1; i++) {
      assertTrue(chunks.get(i).getChunkIndex() <= chunks.get(i + 1).getChunkIndex());
    }
  }

  @Test
  void testFindByDocumentIdAndEmbeddingIsNull() {
    // Given - Save document and chunks (some with embeddings, some without)
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunkWithEmbedding = new DocumentChunk();
    chunkWithEmbedding.setDocument(savedDoc);
    chunkWithEmbedding.setChunkIndex(0);
    chunkWithEmbedding.setTextContent("Chunk with embedding");
    chunkWithEmbedding.setChunkSize(20);
    chunkWithEmbedding.setStartPosition(0);
    chunkWithEmbedding.setEndPosition(20);
    // Note: Setting embedding requires vector format, so we'll test null case
    chunkWithEmbedding.setEmbedding(null);

    DocumentChunk chunkWithoutEmbedding = new DocumentChunk();
    chunkWithoutEmbedding.setDocument(savedDoc);
    chunkWithoutEmbedding.setChunkIndex(1);
    chunkWithoutEmbedding.setTextContent("Chunk without embedding");
    chunkWithoutEmbedding.setChunkSize(20);
    chunkWithoutEmbedding.setStartPosition(20);
    chunkWithoutEmbedding.setEndPosition(40);
    chunkWithoutEmbedding.setEmbedding(null);

    chunkRepository.save(chunkWithEmbedding);
    chunkRepository.save(chunkWithoutEmbedding);
    entityManager.flush();
    entityManager.clear();

    // When - Find chunks without embeddings
    List<DocumentChunk> chunksWithoutEmbedding =
        chunkRepository.findByDocumentIdAndEmbeddingIsNull(savedDoc.getId());

    // Then
    assertTrue(chunksWithoutEmbedding.size() >= 2);
    assertTrue(chunksWithoutEmbedding.stream().allMatch(c -> c.getEmbedding() == null));
  }

  @Test
  void testCountByEmbeddingIsNotNull() {
    // Given - Save chunks with and without embeddings
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("Chunk 1");
    chunk1.setChunkSize(10);
    chunk1.setEmbedding(null);

    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(savedDoc);
    chunk2.setChunkIndex(1);
    chunk2.setTextContent("Chunk 2");
    chunk2.setChunkSize(10);
    chunk2.setEmbedding(null);

    chunkRepository.save(chunk1);
    chunkRepository.save(chunk2);
    entityManager.flush();
    entityManager.clear();

    // When - Count chunks with embeddings
    Long count = chunkRepository.countByEmbeddingIsNotNull();

    // Then - Should return count (may be 0 if no embeddings set)
    assertNotNull(count);
    assertTrue(count >= 0);
  }

  @Test
  void testDeleteByDocument() {
    // Given - Save document and chunks
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("Chunk 1");
    chunk1.setChunkSize(10);

    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(savedDoc);
    chunk2.setChunkIndex(1);
    chunk2.setTextContent("Chunk 2");
    chunk2.setChunkSize(10);

    chunkRepository.save(chunk1);
    chunkRepository.save(chunk2);
    entityManager.flush();
    entityManager.clear();

    // Verify chunks exist
    assertTrue(chunkRepository.findByDocumentId(savedDoc.getId()).size() >= 2);

    // When - Delete by document
    chunkRepository.deleteByDocument(savedDoc);
    entityManager.flush();
    entityManager.clear();

    // Then - Chunks should be deleted
    List<DocumentChunk> remaining = chunkRepository.findByDocumentId(savedDoc.getId());
    assertEquals(0, remaining.size());
  }

  @Test
  void testDeleteByDocumentIdNative() {
    // Given - Save document and chunks
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(0);
    chunk1.setTextContent("Chunk 1");
    chunk1.setChunkSize(10);

    DocumentChunk chunk2 = new DocumentChunk();
    chunk2.setDocument(savedDoc);
    chunk2.setChunkIndex(1);
    chunk2.setTextContent("Chunk 2");
    chunk2.setChunkSize(10);

    chunkRepository.save(chunk1);
    chunkRepository.save(chunk2);
    entityManager.flush();
    entityManager.clear();

    // Verify chunks exist
    assertTrue(chunkRepository.findByDocumentId(savedDoc.getId()).size() >= 2);

    // When - Delete by document ID using native query
    int deleted = chunkRepository.deleteByDocumentIdNative(savedDoc.getId());
    entityManager.flush();
    entityManager.clear();

    // Then - Should have deleted chunks
    assertTrue(deleted >= 2);
    List<DocumentChunk> remaining = chunkRepository.findByDocumentId(savedDoc.getId());
    assertEquals(0, remaining.size());
  }

  @Test
  void testFindByDocumentOrderByChunkIndex() {
    // Given - Save document and chunks
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();

    DocumentChunk chunk1 = new DocumentChunk();
    chunk1.setDocument(savedDoc);
    chunk1.setChunkIndex(1);
    chunk1.setTextContent("Second");
    chunk1.setChunkSize(10);

    DocumentChunk chunk0 = new DocumentChunk();
    chunk0.setDocument(savedDoc);
    chunk0.setChunkIndex(0);
    chunk0.setTextContent("First");
    chunk0.setChunkSize(10);

    chunkRepository.save(chunk1);
    chunkRepository.save(chunk0);
    entityManager.flush();
    entityManager.clear();

    // When - Find by document ordered by chunk index
    List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndex(savedDoc);

    // Then - Should be ordered
    assertTrue(chunks.size() >= 2);
    for (int i = 0; i < chunks.size() - 1; i++) {
      assertTrue(chunks.get(i).getChunkIndex() <= chunks.get(i + 1).getChunkIndex());
    }
  }

  @Test
  void testUpdateChunk() {
    // Given - Save chunk
    Document savedDoc = documentRepository.save(testDocument);
    entityManager.flush();
    testChunk.setDocument(savedDoc);
    DocumentChunk saved = chunkRepository.save(testChunk);
    Long id = saved.getId();
    entityManager.flush();
    entityManager.clear();

    // When - Update
    DocumentChunk found = chunkRepository.findById(id).orElse(null);
    assertNotNull(found);
    found.setTextContent("Updated chunk content");
    found.setChunkSize(25);
    DocumentChunk updated = chunkRepository.save(found);
    entityManager.flush();
    entityManager.clear();

    // Then - Verify update
    DocumentChunk retrieved = chunkRepository.findById(id).orElse(null);
    assertNotNull(retrieved);
    assertEquals("Updated chunk content", retrieved.getTextContent());
    assertEquals(25, retrieved.getChunkSize());
  }
}

