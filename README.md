# 4156-Group-Project

## AI Knowledge Management Service with RAG Implementation

The AI Knowledge Management Service provides intelligent document processing, semantic search, and knowledge extraction capabilities using RAG (Retrieval-Augmented Generation) technology with Spring AI, Ollama, and PostgreSQL + PGVector.

## Overview

This service implements a complete RAG pipeline that:

- Processes documents through AI-powered text extraction and chunking
- Generates embeddings using Ollama's llama3.2 model
- Stores vectors in PostgreSQL with PGVector extension
- Provides semantic search and RAG-enhanced chat capabilities
- Maintains backward compatibility with existing API endpoints

## Prerequisites

1. **Docker and Docker Compose** - Required to run Ollama and PostgreSQL with PGVector
2. **Java 17** - Required for Spring Boot 3.4.4
3. **Maven** - For building the project

## Quick Start

### 1. Start the Services

```bash
# Start Ollama and PostgreSQL with PGVector
docker-compose up -d

# Wait for services to be ready (check logs)
docker-compose logs -f
```

### 2. Pull the Ollama Model

```bash
# Pull the llama3.2 model (this may take a few minutes)
docker exec ollama ollama pull llama3.2
```

### 3. Build and Run the Application

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

## Configuration

The application is configured in `src/main/resources/application.yml`:

- **Ollama**: Running on `http://localhost:11434/`
- **PostgreSQL**: Running on `localhost:5432` with database `knowledge_db`
- **Vector Store**: Uses PGVector with HNSW index and cosine distance
- **Model**: llama3.2 for both chat and embeddings (4096 dimensions)

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. In production, implement proper security measures.

## API Endpoints

### Document Management

#### Upload Document (Enhanced with RAG)

**POST** `/documents`

Upload and process a document through the complete AI pipeline. **Now automatically ingests content into RAG vector store.**

**Request:**

- Content-Type: `multipart/form-data`
- Body: `file` (multipart file)

**Supported Formats:**

- PDF, DOC, DOCX, TXT, HTML, PPT, PPTX, RTF, ODT, ODP

**Response:**

```json
{
  "message": "Document uploaded and processed successfully",
  "documentId": 1,
  "filename": "document.pdf",
  "status": "COMPLETED"
}
```

#### Get Document Details

**GET** `/documents/{id}`

Retrieve a specific document by its ID.

**Response:**

```json
{
  "id": 1,
  "filename": "document.pdf",
  "contentType": "application/pdf",
  "fileSize": 1024,
  "extractedText": "Document content...",
  "summary": "Document summary...",
  "processingStatus": "COMPLETED",
  "uploadedAt": "2024-01-01T10:00:00Z"
}
```

#### Get Document Summary

**GET** `/documents/{id}/summary`

Retrieve generated summary for a document.

**Response:**

```json
{
  "documentId": 1,
  "summary": "Document summary..."
}
```

#### Get Document Relationships

**GET** `/relationships/{documentId}`

Retrieve related documents (knowledge graph edges).

**Response:**

```json
{
  "documentId": 1,
  "relationships": [],
  "count": 0,
  "message": "Relationship analysis not yet implemented"
}
```

### Search Operations

#### Semantic Search (Original)

**GET** `/search/{text}`

Retrieve top 3 relevant documents based on text input using original embedding search.

**Response:**

```json
{
  "query": "machine learning",
  "results": [
    {
      "id": 1,
      "textContent": "Relevant text chunk...",
      "document": {
        "id": 1,
        "filename": "ml_paper.pdf"
      }
    }
  ],
  "count": 3,
  "message": "Search completed successfully"
}
```

#### RAG Vector Search (New)

**GET** `/rag/search`

Search similar documents using RAG vector store.

**Query Parameters:**

- `query` (required): Search query
- `topK` (optional, default: 5): Number of results to return

**Response:**

```json
{
  "query": "machine learning",
  "results": [
    {
      "id": "doc1",
      "content": "Document content...",
      "metadata": {
        "documentId": 1,
        "filename": "ml_paper.pdf"
      }
    }
  ],
  "count": 5,
  "message": "RAG search completed successfully"
}
```

### Chat Operations (New RAG Features)

#### Chat with RAG Context

**POST** `/chat`

Chat with RAG context retrieval from uploaded documents.

**Request:**

```json
{
  "question": "What is the main topic of the uploaded documents?"
}
```

**Response:**

```json
{
  "question": "What is the main topic of the uploaded documents?",
  "response": "Based on the uploaded documents, the main topics include...",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### Direct Chat (without RAG)

**POST** `/chat/direct`

Direct chat without RAG context retrieval.

**Request:**

```json
{
  "question": "Explain machine learning"
}
```

**Response:**

```json
{
  "question": "Explain machine learning",
  "response": "Machine learning is a subset of artificial intelligence...",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### System Information

#### RAG Statistics

**GET** `/rag/stats`

Get RAG vector store statistics.

**Response:**

```json
{
  "status": "active",
  "provider": "PGVector",
  "model": "llama3.2",
  "dimensions": 4096
}
```

#### Embedding Test Connection

**GET** `/embedding/test-connection`

Test Ollama connection for embedding generation.

**Response:**

```json
{
  "status": "success",
  "message": "Ollama connection successful",
  "testText": "This is a test for Ollama embedding generation.",
  "embeddingGenerated": true,
  "embeddingDimensions": 4096
}
```

## Error Responses

All endpoints may return the following error responses:

**400 Bad Request:**

```json
{
  "error": "Invalid request parameters"
}
```

**404 Not Found:**

```json
{
  "error": "Resource not found"
}
```

**500 Internal Server Error:**

```json
{
  "error": "Internal server error"
}
```

## File Size Limits

- Maximum file size: 50MB
- Supported file types: PDF, DOC, DOCX, TXT, HTML, PPT, PPTX, RTF, ODT, ODP

## Processing Pipeline

1. **Upload**: File is uploaded and validated
2. **Text Extraction**: Apache Tika extracts text content
3. **Chunking**: Text is split into meaningful segments
4. **Embedding**: Vector embeddings are generated using Ollama llama3.2 model
5. **RAG Ingestion**: Document content is automatically ingested into RAG vector store
6. **Summarization**: AI-generated summary is created
7. **Completion**: Document is marked as processed

## Key Features

### RAG Implementation

- **Spring AI Integration**: Uses Spring AI framework for AI operations
- **Ollama Local Models**: Runs llama3.2 model locally for embeddings and chat
- **PGVector Storage**: PostgreSQL with PGVector extension for vector storage
- **Automatic Ingestion**: Document uploads automatically populate RAG vector store
- **Semantic Search**: Both original and RAG-enhanced search capabilities
- **Flexible Chat**: Choose between RAG-enhanced or direct chat

### Backward Compatibility

- All existing API endpoints continue to work
- Enhanced functionality without breaking changes
- Seamless integration with existing workflows

## Examples

### Upload a Document (with automatic RAG ingestion)

```bash
curl -X POST http://localhost:8080/api/v1/documents \
  -F "file=@document.pdf"
```

### Chat with RAG Context

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the main topic of the uploaded documents?"
  }'
```

### Direct Chat (without RAG)

```bash
curl -X POST http://localhost:8080/api/v1/chat/direct \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Explain machine learning"
  }'
```

### Search Documents (Original)

```bash
curl http://localhost:8080/api/v1/search/machine%20learning
```

### RAG Vector Search

```bash
curl "http://localhost:8080/api/v1/rag/search?query=machine learning&topK=5"
```

### Get RAG Statistics

```bash
curl http://localhost:8080/api/v1/rag/stats
```

### Test Ollama Connection

```bash
curl http://localhost:8080/api/embedding/test-connection
```

## Troubleshooting

### Ollama Issues

- Ensure Ollama container is running: `docker ps`
- Check Ollama logs: `docker logs ollama`
- Verify model is pulled: `docker exec ollama ollama list`

### PostgreSQL Issues

- Ensure PostgreSQL container is running: `docker ps`
- Check PostgreSQL logs: `docker logs postgres-pgvector`
- Verify PGVector extension: Connect to database and run `SELECT * FROM pg_extension WHERE extname = 'vector';`

### Application Issues

- Check application logs for Spring AI configuration errors
- Verify database connection in `application.yml`
- Ensure all dependencies are properly resolved

## Performance Notes

- **Embedding Generation**: llama3.2 produces 4096-dimensional embeddings
- **Vector Search**: Uses HNSW index for better query performance
- **Batch Processing**: Configured for token-based batching with max 10,000 documents per batch
- **Memory Usage**: Ollama may require significant memory depending on model size
- **Automatic Ingestion**: Document uploads now include RAG ingestion (may take slightly longer)

## Testing

Our unit tests are located under the directory `src/test`. To run our project's tests using Java 17, you must first build the project.

From there, you can right-click any of the classes present in the src/test directory and click run to see the results.

To run all tests from the terminal:
```bash
mvn clean test
```

### Coverage Analysis

To generate test coverage:
```bash
mvn test jacoco:report
```

View results: Open `target/site/jacoco/index.html` in browser

This repository achieves 58% branch coverage (exceeds 55% requirement).

### Test Files

- `DocumentControllerTest.java`: API endpoint testing
- `SimpleEmbeddingServiceTest.java`: Embedding service with mocked dependencies  
- `DocumentServiceTest.java`: Document processing pipeline verification
- `DocumentChunkTest.java`: Model validation and equals/hashCode testing
- `DocumentRelationshipTest.java`: Relationship model testing
- `RagServiceTest.java`: RAG functionality testing
- `DocumentTest.java`: Document model validation
- `FloatArrayToPgVectorConverterTest.java`: Converter testing

### Testing Framework

**Unit Testing:** JUnit 5 with Mockito for mocking
**Coverage Tool:** JaCoCo Maven Plugin  
**API Testing:** Spring Boot Test with MockMvc

**Test Structure:**
- Controller Tests: `src/test/java/dev/coms4156/project/controller/`
- Service Tests: `src/test/java/dev/coms4156/project/service/`
- Model Tests: `src/test/java/dev/coms4156/project/model/`
- Converter Tests: `src/test/java/dev/coms4156/project/converter/`

## Technology Stack

- **Backend**: Spring Boot 3.4.4, Java 17
- **AI Framework**: Spring AI 1.0.3
- **LLM/Embeddings**: Ollama with llama3.2 model
- **Vector Database**: PostgreSQL with PGVector extension
- **Text Processing**: Apache Tika
- **Build Tool**: Maven
- **Containerization**: Docker Compose
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Coverage**: JaCoCo
- **Style Checking**: TODO

## Citations

- https://spring.io/guides/gs/accessing-data-jpa
- https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/
- https://tika.apache.org/2.0.0/examples.html
- https://www.geeksforgeeks.org/java/hibernate-example-using-jpa-and-mysql/
- https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- https://projectlombok.org/api/lombok/package-summary
- https://docs.spring.io/spring-data/jpa/docs/1.6.0.RELEASE/reference/html/jpa.repositories.html
- https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html
- https://docs.spring.io/spring-ai/reference/api/ollama.html
