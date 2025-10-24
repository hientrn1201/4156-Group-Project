# 4156-Group-Project

## AI Knowledge Management Service

The AI Knowledge Management Service provides intelligent document processing, semantic search, and knowledge extraction capabilities using Spring AI, Ollama, and PostgreSQL + PGVector.

## Project Management

We used JIRA for the project management part which can be seen in the link below:

https://cn2673.atlassian.net/jira/software/projects/KAN/boards/1

## Overview

This service implements a complete AI-powered document processing pipeline that:

- Processes documents through AI-powered text extraction and chunking
- Generates embeddings using Ollama's llama3.2 model
- Stores vectors in PostgreSQL with PGVector extension
- Provides semantic search capabilities across document chunks
- Generates AI-powered document summaries
- Offers comprehensive document management operations

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
- **Model**: llama3.2 for both chat and embeddings (3072 dimensions)

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. In production, implement proper security measures.

## API Endpoints

### Document Management

#### Upload Document

**POST** `/documents`

Upload and process a document through the complete AI pipeline.

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

#### Get All Documents

**GET** `/documents`

Retrieve all documents or filter by filename.

**Query Parameters:**

- `filename` (optional): Filter documents by filename

**Response:**

```json
{
  "documents": [
    {
      "id": 1,
      "filename": "document.pdf",
      "contentType": "application/pdf",
      "fileSize": 1024,
      "processingStatus": "COMPLETED",
      "summary": "Document summary...",
      "uploadedAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:05:00Z"
    }
  ],
  "count": 1,
  "message": "Documents retrieved"
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
  "processingStatus": "COMPLETED",
  "summary": "Document summary...",
  "uploadedAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:05:00Z"
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

#### Get Documents with Summaries

**GET** `/documents/summaries`

Retrieve all documents that have summaries generated.

**Response:**

```json
{
  "documents": [
    {
      "id": 1,
      "filename": "document.pdf",
      "summary": "Document summary...",
      "processingStatus": "COMPLETED"
    }
  ],
  "count": 1
}
```

#### Get Processing Statistics

**GET** `/documents/stats`

Get processing statistics for all documents.

**Response:**

```json
{
  "total": 10,
  "byStatus": {
    "UPLOADED": 1,
    "TEXT_EXTRACTED": 2,
    "CHUNKED": 2,
    "EMBEDDINGS_GENERATED": 2,
    "SUMMARIZED": 1,
    "COMPLETED": 2,
    "FAILED": 0
  },
  "completionRate": 0.2,
  "failureRate": 0.0
}
```

#### Delete Document

**DELETE** `/documents/{id}`

Delete a document by its ID.

**Response:**

```json
{
  "documentId": 1,
  "message": "Document deleted successfully"
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

#### Semantic Search

**GET** `/search/{text}`

Retrieve top 3 relevant document chunks based on text input using embedding similarity search.

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

### System Information

#### Welcome Message

**GET** `/api` or `/api/`

Get the welcome message for the Knowledge Management Service.

**Response:**

```
Welcome to Knowledge Management Service Powered by AI!
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
5. **Summarization**: AI-generated summary is created
6. **Completion**: Document is marked as processed

## Key Features

### AI-Powered Document Processing

- **Spring AI Integration**: Uses Spring AI framework for AI operations
- **Ollama Local Models**: Runs llama3.2 model locally for embeddings
- **PGVector Storage**: PostgreSQL with PGVector extension for vector storage
- **Semantic Search**: Embedding-based similarity search across document chunks
- **Intelligent Summarization**: AI-generated document summaries
- **Multi-format Support**: Handles PDF, DOC, DOCX, TXT, HTML, PPT, PPTX, RTF, ODT, ODP

### Document Management

- **Complete CRUD Operations**: Upload, retrieve, update, and delete documents
- **Processing Status Tracking**: Monitor document processing pipeline status
- **Batch Operations**: Process multiple documents efficiently
- **Statistics and Analytics**: Comprehensive processing statistics and metrics

## Examples

### Upload a Document

```bash
curl -X POST http://localhost:8080/api/v1/documents \
  -F "file=@document.pdf"
```

### Get All Documents

```bash
curl http://localhost:8080/api/v1/documents
```

### Get Document by ID

```bash
curl http://localhost:8080/api/v1/documents/1
```

### Get Document Summary

```bash
curl http://localhost:8080/api/v1/documents/1/summary
```

### Get Documents with Summaries

```bash
curl http://localhost:8080/api/v1/documents/summaries
```

### Get Processing Statistics

```bash
curl http://localhost:8080/api/v1/documents/stats
```

### Delete Document

```bash
curl -X DELETE http://localhost:8080/api/v1/documents/1
```

### Search Documents

```bash
curl http://localhost:8080/api/v1/search/machine%20learning
```

### Get Document Relationships

```bash
curl http://localhost:8080/api/v1/relationships/1
```

### Get Welcome Message

```bash
curl http://localhost:8080/api
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

- **Embedding Generation**: llama3.2 produces 3072-dimensional embeddings
- **Vector Search**: Uses HNSW index for better query performance
- **Batch Processing**: Configured for efficient document processing
- **Memory Usage**: Ollama may require significant memory depending on model size
- **Database Optimization**: PostgreSQL with PGVector extension for optimal vector operations

## Testing

Our unit tests are located under the directory `src/test`. To run our project's tests using Java 17, you must first build the project.

From there, you can right-click any of the classes present in the src/test directory and click run to see the results.

To run all tests from the terminal:

```bash
mvn clean test
```

### Coverage Analysis

An existing coverage report can be viewed by opening `target/site/jacoco/index.html` in the browser, however if you would like to re-generate it, run the following command:

```bash
mvn test jacoco:report
```

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
- **AI Framework**: Spring AI 1.0.0
- **LLM/Embeddings**: Ollama with llama3.2 model
- **Vector Database**: PostgreSQL with PGVector extension
- **Text Processing**: Apache Tika
- **Build Tool**: Maven
- **Containerization**: Docker Compose
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Coverage**: JaCoCo
- **Style Checking**: Style Checking:Checkstyle with Google Java Style
- **Static Analysis**: PMD
## CI/CD Pipeline

This project includes an automated CI pipeline that runs on pushes and pull requests to the main branch.

### What the Pipeline Does

1. **Builds the project** with Java 17 and Maven
2. **Runs unit tests** against PostgreSQL with PGVector (skips tests that require Ollama)
3. **Runs quality checks** Checkstyle, PMD, JaCoCo coverage
4. **Generates artifacts** (test results, coverage reports, application JAR)

### Pipeline Triggers

- Push to `main` branch
- Pull requests to `main` branch

### Database Testing

Uses PostgreSQL with PGVector extension for testing database connectivity and basic operations.

### Artifacts Generated

- Test results from Surefire reports
- Coverage reports from JaCoCo
- Application JAR file

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

## AI usage documentation:

**Tools : ChatGPT Free plan**

**Used in:**

**Examples**
- **Help with Docker and config files to properly set up docker and host locally**

  Reason: We are new to Docker and try to follow resources online but does not work out

  Prompt: I’m running docker compose up -d and I get:

  ERROR: Cannot create container for service app: Mounts denied: path … not found

- **Helping with refining phrasing in Java docs/ API/test documentation**

  Reason: AI can propose better, concise documentation that help the code to be more clear (use as references)

  Prompt (similar to this): Here is a Javadoc, make it more clear and concise

- **help with REGEX**

  Reason: REGEX is painful to deal with and AI is extremely helpful with this

  Prompt: how to clean raw text by removing whitespace and normalize line break using regex

- **Write testing script – feel like a nice automation (not related to main codebase)**

  Reason: this is entirely optional and a nice thing to have when development 

  Prompt: write me a short bash script to run tests using the README

- **Debugging Spring AI / RAG retrieval not successful because log statement was lengthy**

  Reason: We are new to Spring AI and RAG retrieve in Java so when the error log was long, we were confused because we thought we did everything right.
  We used it to help with Spring AI implementations and errors associated when building out.

  Prompt: I got this error [long error log] when retrieve relevant text using RAG
