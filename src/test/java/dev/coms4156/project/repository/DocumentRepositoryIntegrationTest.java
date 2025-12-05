package dev.coms4156.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.model.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for DocumentRepository.
 * Tests actual database operations using PostgreSQL with PGVector.
 *
 * <p>This test integrates with:
 * - PostgreSQL database (external resource)
 * - JPA/Hibernate ORM
 * - Document entity persistence
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.show-sql=false"
})
class DocumentRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private DocumentRepository documentRepository;

  private Document testDocument;

  @BeforeEach
  void setUp() {
    // Clean up any existing test data
    documentRepository.deleteAll();
    entityManager.flush();
    entityManager.clear();

    // Create a test document
    testDocument = new Document();
    testDocument.setFilename("test-document.pdf");
    testDocument.setContentType("application/pdf");
    testDocument.setFileSize(1024L);
    testDocument.setExtractedText("This is test content for integration testing.");
    testDocument.setSummary("Test summary");
    testDocument.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    testDocument.setUploadedAt(LocalDateTime.now());
    testDocument.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void testSaveAndFindById() {
    // When - Save document
    Document saved = documentRepository.save(testDocument);
    entityManager.flush();
    entityManager.clear();

    // Then - Retrieve by ID
    Optional<Document> found = documentRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals("test-document.pdf", found.get().getFilename());
    assertEquals("application/pdf", found.get().getContentType());
    assertEquals(1024L, found.get().getFileSize());
    assertEquals(Document.ProcessingStatus.COMPLETED, found.get().getProcessingStatus());
  }

  @Test
  void testFindByProcessingStatus() {
    // Given - Create documents with different statuses
    Document doc1 = new Document();
    doc1.setFilename("doc1.pdf");
    doc1.setContentType("application/pdf");
    doc1.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    doc1.setUploadedAt(LocalDateTime.now());

    Document doc2 = new Document();
    doc2.setFilename("doc2.pdf");
    doc2.setContentType("application/pdf");
    doc2.setProcessingStatus(Document.ProcessingStatus.FAILED);
    doc2.setUploadedAt(LocalDateTime.now());

    Document doc3 = new Document();
    doc3.setFilename("doc3.pdf");
    doc3.setContentType("application/pdf");
    doc3.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    doc3.setUploadedAt(LocalDateTime.now());

    documentRepository.save(doc1);
    documentRepository.save(doc2);
    documentRepository.save(doc3);
    entityManager.flush();
    entityManager.clear();

    // When - Find by status
    List<Document> completed = documentRepository.findByProcessingStatus(
        Document.ProcessingStatus.COMPLETED);
    List<Document> failed = documentRepository.findByProcessingStatus(
        Document.ProcessingStatus.FAILED);

    // Then
    assertTrue(completed.size() >= 2);
    assertTrue(failed.size() >= 1);
    assertTrue(completed.stream().allMatch(
        d -> d.getProcessingStatus() == Document.ProcessingStatus.COMPLETED));
    assertTrue(failed.stream().allMatch(
        d -> d.getProcessingStatus() == Document.ProcessingStatus.FAILED));
  }

  @Test
  void testFindByContentType() {
    // Given - Create documents with different content types
    Document pdfDoc = new Document();
    pdfDoc.setFilename("test.pdf");
    pdfDoc.setContentType("application/pdf");
    pdfDoc.setUploadedAt(LocalDateTime.now());

    Document txtDoc = new Document();
    txtDoc.setFilename("test.txt");
    txtDoc.setContentType("text/plain");
    txtDoc.setUploadedAt(LocalDateTime.now());

    documentRepository.save(pdfDoc);
    documentRepository.save(txtDoc);
    entityManager.flush();
    entityManager.clear();

    // When - Find by content type
    List<Document> pdfDocs = documentRepository.findByContentType("application/pdf");
    List<Document> txtDocs = documentRepository.findByContentType("text/plain");

    // Then
    assertTrue(pdfDocs.size() >= 1);
    assertTrue(txtDocs.size() >= 1);
    assertTrue(pdfDocs.stream().allMatch(d -> d.getContentType().equals("application/pdf")));
    assertTrue(txtDocs.stream().allMatch(d -> d.getContentType().equals("text/plain")));
  }

  @Test
  void testFindByFilenameContaining() {
    // Given - Create documents with different filenames
    Document doc1 = new Document();
    doc1.setFilename("research-paper.pdf");
    doc1.setContentType("application/pdf");
    doc1.setUploadedAt(LocalDateTime.now());

    Document doc2 = new Document();
    doc2.setFilename("research-summary.pdf");
    doc2.setContentType("application/pdf");
    doc2.setUploadedAt(LocalDateTime.now());

    Document doc3 = new Document();
    doc3.setFilename("unrelated-doc.pdf");
    doc3.setContentType("application/pdf");
    doc3.setUploadedAt(LocalDateTime.now());

    documentRepository.save(doc1);
    documentRepository.save(doc2);
    documentRepository.save(doc3);
    entityManager.flush();
    entityManager.clear();

    // When - Find by filename containing
    List<Document> researchDocs = documentRepository.findByFilenameContaining("research");

    // Then
    assertTrue(researchDocs.size() >= 2);
    assertTrue(researchDocs.stream().allMatch(
        d -> d.getFilename().contains("research")));
  }

  @Test
  void testFindDocumentsWithSummaries() {
    // Given - Create documents with and without summaries
    Document withSummary = new Document();
    withSummary.setFilename("doc1.pdf");
    withSummary.setContentType("application/pdf");
    withSummary.setSummary("This document has a summary");
    withSummary.setUploadedAt(LocalDateTime.now());

    Document withoutSummary = new Document();
    withoutSummary.setFilename("doc2.pdf");
    withoutSummary.setContentType("application/pdf");
    withoutSummary.setSummary(null);
    withoutSummary.setUploadedAt(LocalDateTime.now());

    Document withEmptySummary = new Document();
    withEmptySummary.setFilename("doc3.pdf");
    withEmptySummary.setContentType("application/pdf");
    withEmptySummary.setSummary("");
    withEmptySummary.setUploadedAt(LocalDateTime.now());

    Document savedWithSummary = documentRepository.save(withSummary);
    documentRepository.save(withoutSummary);
    documentRepository.save(withEmptySummary);
    entityManager.flush();
    entityManager.clear();

    // When - Find documents with summaries
    List<Document> docsWithSummaries = documentRepository.findDocumentsWithSummaries();

    // Then - Should include documents with non-null summaries (including empty strings)
    // The query returns documents where summary IS NOT NULL, which includes empty strings
    assertTrue(docsWithSummaries.size() >= 2, 
        "Expected at least 2 documents (withSummary and withEmptySummary)");
    // Verify the document with actual summary is included
    assertTrue(docsWithSummaries.stream().anyMatch(
        d -> d.getId().equals(savedWithSummary.getId()) 
            && "This document has a summary".equals(d.getSummary())));
    // All returned documents should have non-null summaries
    assertTrue(docsWithSummaries.stream().allMatch(
        d -> d.getSummary() != null));
  }

  @Test
  void testCountByProcessingStatus() {
    // Given - Create documents with different statuses
    Document doc1 = new Document();
    doc1.setFilename("doc1.pdf");
    doc1.setContentType("application/pdf");
    doc1.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    doc1.setUploadedAt(LocalDateTime.now());

    Document doc2 = new Document();
    doc2.setFilename("doc2.pdf");
    doc2.setContentType("application/pdf");
    doc2.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
    doc2.setUploadedAt(LocalDateTime.now());

    Document doc3 = new Document();
    doc3.setFilename("doc3.pdf");
    doc3.setContentType("application/pdf");
    doc3.setProcessingStatus(Document.ProcessingStatus.FAILED);
    doc3.setUploadedAt(LocalDateTime.now());

    documentRepository.save(doc1);
    documentRepository.save(doc2);
    documentRepository.save(doc3);
    entityManager.flush();
    entityManager.clear();

    // When - Count by status
    Long completedCount = documentRepository.countByProcessingStatus(
        Document.ProcessingStatus.COMPLETED);
    Long failedCount = documentRepository.countByProcessingStatus(
        Document.ProcessingStatus.FAILED);

    // Then
    assertTrue(completedCount >= 2);
    assertTrue(failedCount >= 1);
  }

  @Test
  void testDelete() {
    // Given - Save a document
    Document saved = documentRepository.save(testDocument);
    Long id = saved.getId();
    entityManager.flush();
    entityManager.clear();

    // Verify it exists
    assertTrue(documentRepository.findById(id).isPresent());

    // When - Delete
    documentRepository.deleteById(id);
    entityManager.flush();
    entityManager.clear();

    // Then - Should not exist
    assertFalse(documentRepository.findById(id).isPresent());
  }

  @Test
  void testFindAll() {
    // Given - Save multiple documents
    Document doc1 = new Document();
    doc1.setFilename("doc1.pdf");
    doc1.setContentType("application/pdf");
    doc1.setUploadedAt(LocalDateTime.now());

    Document doc2 = new Document();
    doc2.setFilename("doc2.pdf");
    doc2.setContentType("application/pdf");
    doc2.setUploadedAt(LocalDateTime.now());

    documentRepository.save(doc1);
    documentRepository.save(doc2);
    entityManager.flush();
    entityManager.clear();

    // When - Find all
    List<Document> all = documentRepository.findAll();

    // Then
    assertTrue(all.size() >= 2);
  }

  @Test
  void testUpdateDocument() {
    // Given - Save a document
    Document saved = documentRepository.save(testDocument);
    Long id = saved.getId();
    entityManager.flush();
    entityManager.clear();

    // When - Update
    Optional<Document> found = documentRepository.findById(id);
    assertTrue(found.isPresent());
    Document doc = found.get();
    doc.setFilename("updated-filename.pdf");
    doc.setSummary("Updated summary");
    doc.setUpdatedAt(LocalDateTime.now());
    Document updated = documentRepository.save(doc);
    entityManager.flush();
    entityManager.clear();

    // Then - Verify update
    Optional<Document> retrieved = documentRepository.findById(id);
    assertTrue(retrieved.isPresent());
    assertEquals("updated-filename.pdf", retrieved.get().getFilename());
    assertEquals("Updated summary", retrieved.get().getSummary());
  }
}

