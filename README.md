# 4156-Group-Project

citations:

https://spring.io/guides/gs/accessing-data-jpa

https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/

https://tika.apache.org/2.0.0/examples.html

https://www.geeksforgeeks.org/java/hibernate-example-using-jpa-and-mysql/

https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

https://projectlombok.org/api/lombok/package-summary

https://docs.spring.io/spring-data/jpa/docs/1.6.0.RELEASE/reference/html/jpa.repositories.html

# AI Knowledge Management Service API Documentation

## Overview

The AI Knowledge Management Service provides intelligent document processing, semantic search, and knowledge extraction capabilities using RAG (Retrieval-Augmented Generation) technology.

## Base URL

```
http://localhost:8080/api
```

## Authentication

Currently, the API does not require authentication. In production, implement proper security measures.

## API Endpoints

### Document Management

#### Upload Document

**POST** `/documents/upload`

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

Retrieve all documents in the system.

**Response:**

```json
[
  {
    "id": 1,
    "filename": "document.pdf",
    "contentType": "application/pdf",
    "fileSize": 1024,
    "processingStatus": "COMPLETED",
    "uploadedAt": "2024-01-01T10:00:00Z"
  }
]
```

#### Get Document by ID

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

#### Get Documents by Status

**GET** `/documents/status/{status}`

Get documents filtered by processing status.

**Status Values:**

- `UPLOADED`
- `TEXT_EXTRACTED`
- `CHUNKED`
- `EMBEDDED`
- `SUMMARIZED`
- `COMPLETED`
- `FAILED`

#### Get Documents with Summaries

**GET** `/documents/summaries`

Retrieve all documents that have been summarized.

#### Delete Document

**DELETE** `/documents/{id}`

Delete a document and all associated data.

**Response:**

```json
{
  "message": "Document deleted successfully"
}
```

### Search Operations

#### Semantic Search

**POST** `/documents/search`

Perform semantic search across all documents.

**Request:**

```json
{
  "query": "machine learning algorithms",
  "limit": 10
}
```

**Response:**

```json
{
  "query": "machine learning algorithms",
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
  "count": 5
}
```

#### Advanced Semantic Search

**POST** `/search/semantic`

Advanced search with similarity thresholds.

**Request:**

```json
{
  "query": "artificial intelligence",
  "limit": 20,
  "threshold": 0.8
}
```

#### Search in Specific Document

**POST** `/search/document/{documentId}`

Search within a specific document.

**Request:**

```json
{
  "query": "specific topic"
}
```

#### Get Search Suggestions

**GET** `/search/suggestions`

Get search suggestions based on existing content.

**Query Parameters:**

- `prefix` (optional): Filter suggestions by prefix

### Summarization

#### Cross-Document Summary

**POST** `/documents/summarize/cross`

Generate a summary across multiple documents.

**Request:**

```json
{
  "query": "What are the main findings?",
  "context": "Document content from multiple sources..."
}
```

**Response:**

```json
{
  "query": "What are the main findings?",
  "summary": "Comprehensive summary across documents..."
}
```

#### Extract Key Insights

**POST** `/documents/insights`

Extract key insights from text content.

**Request:**

```json
{
  "text": "Long text content to analyze..."
}
```

**Response:**

```json
{
  "insights": "Key insights and findings..."
}
```

### Knowledge Graph

#### Get Document Relationships

**GET** `/knowledge/relationships/document/{documentId}`

Get all relationships for a specific document.

#### Get Relationships by Type

**GET** `/knowledge/relationships/type/{type}`

Get relationships filtered by type.

**Relationship Types:**

- `SEMANTIC_SIMILARITY`
- `TOPICAL_RELATEDNESS`
- `TEMPORAL_SEQUENCE`
- `CAUSAL_RELATIONSHIP`
- `REFERENCE`
- `CONTRAST`
- `EXAMPLE`
- `DEFINITION`

#### Get High-Confidence Relationships

**GET** `/knowledge/relationships/high-confidence`

Get relationships above a confidence threshold.

**Query Parameters:**

- `threshold` (default: 0.8): Minimum confidence score

#### Analyze Document Connections

**GET** `/knowledge/analysis/document/{documentId}`

Analyze connection patterns for a document.

### System Information

#### Get Processing Statistics

**GET** `/documents/stats`

Get system processing statistics.

**Response:**

```json
{
  "total": 100,
  "completed": 95,
  "failed": 2,
  "processing": 3
}
```

#### Get Knowledge Graph Statistics

**GET** `/knowledge/stats`

Get knowledge graph statistics.

#### Get Search Analytics

**GET** `/search/analytics`

Get search usage analytics.

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

## Rate Limiting

Currently no rate limiting is implemented. In production, implement appropriate rate limiting.

## File Size Limits

- Maximum file size: 50MB
- Supported file types: PDF, DOC, DOCX, TXT, HTML, PPT, PPTX, RTF, ODT, ODP

## Processing Pipeline

1. **Upload**: File is uploaded and validated
2. **Text Extraction**: Apache Tika extracts text content
3. **Chunking**: Text is split into meaningful segments
4. **Embedding**: Vector embeddings are generated using Nomic model
5. **Summarization**: AI-generated summary is created
6. **Relationship Analysis**: Document relationships are identified
7. **Completion**: Document is marked as processed

## Setup Instructions

1. Start required services:

```bash
docker-compose up -d
```

2. Pull the Nomic embedding model:

```bash
docker exec ollama_service ollama pull nomic-embed-text
```

3. Run the application:

```bash
mvn spring-boot:run
```

## Examples

### Upload a Document

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@document.pdf"
```

### Search Documents

```bash
curl -X POST http://localhost:8080/api/documents/search \
  -H "Content-Type: application/json" \
  -d '{"query": "machine learning", "limit": 5}'
```

### Get Document Summary

```bash
curl http://localhost:8080/api/documents/1
```
