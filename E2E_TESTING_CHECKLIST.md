# End-to-End Testing Checklist

## Overview
This checklist documents comprehensive end-to-end testing for the AI Knowledge Management Service API. It covers all endpoints, expected outcomes, error scenarios, and edge cases.

## Prerequisites
- Service running at `http://localhost:8080`
- Docker services (Ollama, PostgreSQL) running via `docker-compose up -d`
- Valid JWT token obtained through authentication
- Test files available for upload

## Test Environment Setup

### 1. Service Health Check
```bash
# Verify service is running
curl -s http://localhost:8080/api
# Expected: "Welcome to Knowledge Management Service Powered by AI!"
```

### 2. Authentication Setup
```bash
# Register test user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
# Expected: {"token": "eyJ...", "username": "testuser", "email": "test@example.com"}

# Login existing user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
# Expected: {"token": "eyJ...", "username": "testuser", "email": "test@example.com"}
```

## API Endpoint Testing

### Authentication Endpoints (`/api/v1/auth`)

#### POST /api/v1/auth/register
**Test Cases:**
- [ ] **Valid Registration**: New user with valid data
  - Expected: 200 OK, JWT token returned
- [ ] **Duplicate Username**: Register existing username
  - Expected: 400 Bad Request, error message
- [ ] **Invalid Email**: Register with malformed email
  - Expected: 400 Bad Request, validation error
- [ ] **Missing Fields**: Register without required fields
  - Expected: 400 Bad Request, validation error

#### POST /api/v1/auth/login
**Test Cases:**
- [ ] **Valid Login**: Correct username/password
  - Expected: 200 OK, JWT token returned
- [ ] **Invalid Password**: Wrong password
  - Expected: 401 Unauthorized, error message
- [ ] **Non-existent User**: Login with unknown username
  - Expected: 401 Unauthorized, error message
- [ ] **Missing Credentials**: Login without username/password
  - Expected: 400 Bad Request, validation error

### Welcome Endpoint (`/api`)

#### GET /api
**Test Cases:**
- [ ] **Welcome Message**: Basic service health check
  - Expected: 200 OK, welcome message string
- [ ] **With Authentication**: Include JWT token
  - Expected: 200 OK, same welcome message
- [ ] **Without Authentication**: No JWT token
  - Expected: 200 OK, same welcome message (public endpoint)

### Document Management Endpoints (`/api/v1/documents`)

#### POST /api/v1/documents (Upload Document)
**Test Cases:**
- [ ] **Valid PDF Upload**: Upload .pdf file
  - Expected: 200 OK, document ID, processing status
- [ ] **Valid TXT Upload**: Upload .txt file
  - Expected: 200 OK, document ID, processing status
- [ ] **Valid DOCX Upload**: Upload .docx file
  - Expected: 200 OK, document ID, processing status
- [ ] **Large File Upload**: Upload file near 50MB limit
  - Expected: 200 OK or timeout handling
- [ ] **Unsupported Format**: Upload .exe or other unsupported file
  - Expected: 400 Bad Request, error message
- [ ] **Empty File**: Upload 0-byte file
  - Expected: 400 Bad Request, error message
- [ ] **No File**: POST without file parameter
  - Expected: 400 Bad Request, error message
- [ ] **No Authentication**: Upload without JWT token
  - Expected: 401 Unauthorized

#### GET /api/v1/documents (Get All Documents)
**Test Cases:**
- [ ] **List All Documents**: No filters
  - Expected: 200 OK, array of documents with metadata
- [ ] **Filter by Filename**: Use filename parameter
  - Expected: 200 OK, filtered results
- [ ] **Empty Database**: No documents uploaded
  - Expected: 200 OK, empty array, count: 0
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

#### GET /api/v1/documents/{id} (Get Document by ID)
**Test Cases:**
- [ ] **Valid Document ID**: Existing document
  - Expected: 200 OK, document details with all metadata
- [ ] **Non-existent ID**: ID that doesn't exist
  - Expected: 404 Not Found
- [ ] **Invalid ID Format**: Non-numeric ID
  - Expected: 400 Bad Request
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

#### GET /api/v1/documents/{id}/summary (Get Document Summary)
**Test Cases:**
- [ ] **Document with Summary**: Processed document
  - Expected: 200 OK, document ID and summary text
- [ ] **Document without Summary**: Unprocessed document
  - Expected: 404 Not Found
- [ ] **Non-existent Document**: Invalid document ID
  - Expected: 404 Not Found
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

#### GET /api/v1/documents/summaries (Get Documents with Summaries)
**Test Cases:**
- [ ] **Documents with Summaries**: Some processed documents exist
  - Expected: 200 OK, array of documents that have summaries
- [ ] **No Summaries**: No documents have been summarized
  - Expected: 200 OK, empty array, count: 0
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

#### GET /api/v1/documents/stats (Get Processing Statistics)
**Test Cases:**
- [ ] **With Documents**: Various processing statuses
  - Expected: 200 OK, total count, status breakdown, completion/failure rates
- [ ] **Empty Database**: No documents
  - Expected: 200 OK, total: 0, all status counts: 0, rates: 0.0
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

#### DELETE /api/v1/documents/{id} (Delete Document)
**Test Cases:**
- [ ] **Valid Deletion**: Existing document
  - Expected: 200 OK, confirmation message with document ID
- [ ] **Non-existent Document**: Invalid document ID
  - Expected: 404 Not Found, error message
- [ ] **Already Deleted**: Delete same document twice
  - Expected: 404 Not Found
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

### Search Endpoints (`/api/v1/search`)

#### GET /api/v1/search/{text} (Semantic Search)
**Test Cases:**
- [ ] **Valid Search Query**: Common terms
  - Expected: 200 OK, relevant document chunks, similarity scores
- [ ] **No Results**: Query with no matches
  - Expected: 200 OK, empty results array, count: 0
- [ ] **Special Characters**: Query with symbols, punctuation
  - Expected: 200 OK, handled gracefully
- [ ] **Long Query**: Very long search text
  - Expected: 200 OK or appropriate handling
- [ ] **Empty Query**: Empty string search
  - Expected: 400 Bad Request or empty results
- [ ] **URL Encoding**: Query with spaces and special chars
  - Expected: 200 OK, properly decoded query
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

### Relationship Endpoints (`/api/v1/relationships`)

#### GET /api/v1/relationships/{documentId} (Get Document Relationships)
**Test Cases:**
- [ ] **Valid Document**: Existing document ID
  - Expected: 200 OK, relationships array (may be empty), count
- [ ] **Non-existent Document**: Invalid document ID
  - Expected: 200 OK, empty relationships (current implementation)
- [ ] **No Authentication**: Request without JWT token
  - Expected: 401 Unauthorized

## Error Handling Testing

### HTTP Status Codes
- [ ] **200 OK**: Successful operations
- [ ] **400 Bad Request**: Invalid input, validation errors
- [ ] **401 Unauthorized**: Missing or invalid JWT token
- [ ] **404 Not Found**: Resource not found
- [ ] **500 Internal Server Error**: Server-side errors

### Error Response Format
- [ ] **Consistent Error Structure**: All errors return proper JSON format
- [ ] **Meaningful Error Messages**: Clear, actionable error descriptions
- [ ] **No Sensitive Information**: Errors don't expose internal details

## Performance Testing

### Load Testing
- [ ] **Concurrent Uploads**: Multiple clients uploading simultaneously
- [ ] **Concurrent Searches**: Multiple search requests
- [ ] **Large File Processing**: Upload and process large documents
- [ ] **Database Stress**: Many documents in system

### Timeout Testing
- [ ] **Long Processing**: Documents that take time to process
- [ ] **Search Timeouts**: Complex search queries
- [ ] **Connection Timeouts**: Network interruption scenarios

## Integration Testing

### Document Processing Pipeline
- [ ] **End-to-End Flow**: Upload → Text Extraction → Chunking → Embeddings → Summary
- [ ] **Status Progression**: Verify status updates through pipeline
- [ ] **Error Recovery**: Handle failures at each stage
- [ ] **Data Consistency**: Verify data integrity throughout process

### Multi-Client Testing
- [ ] **Client Isolation**: Multiple clients don't interfere
- [ ] **Concurrent Operations**: Simultaneous operations work correctly
- [ ] **Client Identification**: X-Client-ID header handling

## Security Testing

### Authentication
- [ ] **JWT Validation**: Invalid tokens rejected
- [ ] **Token Expiration**: Expired tokens handled
- [ ] **No Token**: Protected endpoints require authentication
- [ ] **Token Tampering**: Modified tokens rejected

### Authorization
- [ ] **User Isolation**: Users can only access their data
- [ ] **Admin Operations**: Proper permission checks

### Input Validation
- [ ] **SQL Injection**: Malicious input in search queries
- [ ] **File Upload Security**: Malicious file uploads
- [ ] **XSS Prevention**: Script injection in text fields

## Data Validation Testing

### File Upload Validation
- [ ] **File Type Validation**: Only supported formats accepted
- [ ] **File Size Limits**: Enforce maximum file size
- [ ] **File Content Validation**: Verify file integrity

### Search Query Validation
- [ ] **Query Length Limits**: Handle very long queries
- [ ] **Special Character Handling**: Unicode, symbols, etc.
- [ ] **Encoding Issues**: Proper UTF-8 handling

## Manual Testing Scripts

### Quick Smoke Test
```bash
#!/bin/bash
# Run basic functionality test
python3 client/client.py welcome
python3 client/client.py list
python3 client/client.py stats
```

### Full Feature Test
```bash
#!/bin/bash
# Test all major features
python3 client/client.py upload test-document.txt
python3 client/client.py list
python3 client/client.py get 1
python3 client/client.py summary 1
python3 client/client.py search "test"
python3 client/client.py stats
python3 client/client.py delete 1
```

### Multi-Client Test
```bash
#!/bin/bash
# Run multiple client demo
python3 client/demo_multiple_clients.py
```

## Test Data Requirements

### Test Files
- [ ] **Small Text File**: < 1KB for quick tests
- [ ] **Medium PDF**: 1-10MB typical document
- [ ] **Large Document**: Near 50MB limit
- [ ] **Various Formats**: PDF, DOC, DOCX, TXT, HTML, PPT, etc.
- [ ] **Edge Cases**: Empty files, corrupted files, unsupported formats

### Test Users
- [ ] **Valid Test User**: For authentication testing
- [ ] **Multiple Users**: For isolation testing
- [ ] **Invalid Credentials**: For negative testing

## Automated Testing Integration

### CI/CD Pipeline Tests
- [ ] **Unit Tests**: All service layer tests pass
- [ ] **Integration Tests**: Database connectivity tests
- [ ] **API Tests**: Endpoint response validation
- [ ] **Security Tests**: Authentication and authorization

### Test Coverage
- [ ] **Code Coverage**: Maintain >55% branch coverage
- [ ] **API Coverage**: All endpoints tested
- [ ] **Error Path Coverage**: All error scenarios tested

## Test Results Documentation

### Test Execution Log
- [ ] **Test Date/Time**: When tests were executed
- [ ] **Environment**: Service version, dependencies
- [ ] **Test Results**: Pass/fail status for each test case
- [ ] **Issues Found**: Bugs, performance issues, etc.
- [ ] **Recommendations**: Improvements and fixes needed

### Performance Metrics
- [ ] **Response Times**: Average response time per endpoint
- [ ] **Throughput**: Requests per second capacity
- [ ] **Resource Usage**: Memory, CPU, disk usage
- [ ] **Error Rates**: Percentage of failed requests

## Conclusion

This checklist provides comprehensive coverage of all API endpoints and their expected behaviors. Execute these tests manually or integrate them into automated testing frameworks to ensure the AI Knowledge Management Service functions correctly across all scenarios.

**Total Test Cases**: 80+ individual test scenarios covering all endpoints, error conditions, and edge cases.