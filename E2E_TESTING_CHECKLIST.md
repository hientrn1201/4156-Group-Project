# End-to-End Testing Guide

## Overview
This guide provides comprehensive end-to-end testing for our AI Knowledge Management Service. We've designed these tests to verify that clients can successfully use all the core functionality our service provides - from uploading documents to searching through them using AI-powered semantic search.

Our testing approach includes both automated scripts for quick validation and manual test procedures for thorough verification. This ensures we can catch issues whether you're doing rapid development cycles or preparing for production releases.

## Prerequisites
Before running any tests, make sure you have:
- The service running at `http://localhost:8080`
- Docker services started with `docker-compose up -d`
- A valid JWT token (we'll show you how to get one)
- Some test files ready for upload (PDF, TXT, or DOCX work well)

## Getting Started

First, let's verify everything is working:

```bash
# Check if the service is responding
curl -s http://localhost:8080/api
```
You should see: `Welcome to Knowledge Management Service Powered by AI!`

Next, get a JWT token for authenticated requests:

```bash
# Register a new test user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
```

Save the token from the response - you'll need it for most API calls.

## Automated Tests

We've created several automated test runners that exercise the full functionality of our service. These are great for continuous integration and quick validation during development.

### Running Automated Tests

**Option 1: Comprehensive Test Runner**
```bash
# Run the full automated test suite
./run_e2e_tests.sh
```

This script will:
- Verify all services are running
- Test authentication flows
- Upload sample documents
- Test search functionality
- Verify document management operations
- Clean up test data

**Option 2: Python Client Tests**
```bash
cd client
python3 e2e_test_runner.py
```

This Python script provides more detailed output and is useful for debugging specific issues.

**Option 3: Multi-Client Testing**
```bash
cd client
python3 demo_multiple_clients.py
```

This demonstrates how multiple clients can use the service simultaneously, which is important for real-world usage scenarios.

## Manual Testing Procedures

Sometimes you need to test specific scenarios manually or investigate issues in detail. Here are the key test cases you should run through:

### Core Authentication Flow

**Test 1: User Registration**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "email": "new@example.com", "password": "securepass"}'
```
Expected: 200 OK with JWT token

**Test 2: User Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "securepass"}'
```
Expected: 200 OK with JWT token

**Test 3: Invalid Login**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "wrongpass"}'
```
Expected: 401 Unauthorized with error message

### Document Upload and Management

**Test 4: Upload a Document**
```bash
# Replace YOUR_JWT_TOKEN with the token from authentication
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@sample-document.pdf"
```
Expected: 200 OK with document ID and processing status

**Test 5: List All Documents**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents
```
Expected: 200 OK with array of uploaded documents

**Test 6: Get Specific Document**
```bash
# Replace 1 with actual document ID from previous test
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/1
```
Expected: 200 OK with detailed document information

**Test 7: Get Document Summary**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/1/summary
```
Expected: 200 OK with AI-generated summary (may take time to process)

**Test 8: Upload Invalid File**
```bash
# Try uploading an unsupported file type
echo "test" > test.exe
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@test.exe"
```
Expected: 400 Bad Request with error message

### Semantic Search Testing

**Test 9: Basic Search**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/search/machine%20learning"
```
Expected: 200 OK with relevant document chunks

**Test 10: Search with No Results**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/search/nonexistent%20topic"
```
Expected: 200 OK with empty results array

**Test 11: Search with Special Characters**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/search/AI%20%26%20data%20science"
```
Expected: 200 OK with properly handled query

### Document Statistics and Management

**Test 12: Get Processing Statistics**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/stats
```
Expected: 200 OK with processing statistics and completion rates

**Test 13: Get Documents with Summaries**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/summaries
```
Expected: 200 OK with documents that have been summarized

**Test 14: Delete Document**
```bash
# Replace 1 with actual document ID
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -X DELETE http://localhost:8080/api/v1/documents/1
```
Expected: 200 OK with deletion confirmation

### Error Handling Verification

**Test 15: Unauthorized Access**
```bash
# Try accessing protected endpoint without token
curl http://localhost:8080/api/v1/documents
```
Expected: 401 Unauthorized

**Test 16: Invalid Document ID**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/99999
```
Expected: 404 Not Found

**Test 17: Malformed Request**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/documents/invalid-id
```
Expected: 400 Bad Request

### Multi-Client Testing

**Test 18: Concurrent Operations**
Open two terminal windows and run these simultaneously:

Terminal 1:
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Client-ID: client-1" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@document1.pdf"
```

Terminal 2:
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Client-ID: client-2" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@document2.pdf"
```

Both should succeed without interfering with each other.

## Complete End-to-End Workflow Test

This is the most important test - it verifies that a client can use our service for its intended purpose: managing and searching through documents with AI assistance.

**Test 19: Full Client Workflow**

1. **Setup**: Register and authenticate
```bash
# Get your JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "workflow-test", "email": "workflow@test.com", "password": "test123"}' \
  | jq -r '.token')
```

2. **Upload documents**: Add some content to search through
```bash
# Upload multiple documents
curl -H "Authorization: Bearer $TOKEN" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@research-paper.pdf"

curl -H "Authorization: Bearer $TOKEN" \
  -X POST http://localhost:8080/api/v1/documents \
  -F "file=@meeting-notes.txt"
```

3. **Wait for processing**: Check status until complete
```bash
# Check processing status
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/documents/stats
```

4. **Search for information**: Use semantic search to find relevant content
```bash
# Search for specific topics
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/project%20timeline"

curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/research%20findings"
```

5. **Get summaries**: Retrieve AI-generated summaries
```bash
# Get summaries of uploaded documents
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/documents/summaries
```

6. **Cleanup**: Remove test documents
```bash
# List documents to get IDs
DOC_IDS=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/documents | jq -r '.documents[].id')

# Delete each document
for id in $DOC_IDS; do
  curl -H "Authorization: Bearer $TOKEN" \
    -X DELETE http://localhost:8080/api/v1/documents/$id
done
```

## What Success Looks Like

When running these tests, you should see:
- Authentication returns valid JWT tokens
- Document uploads return 200 OK with document IDs
- Processing status progresses from UPLOADED to COMPLETED
- Search queries return relevant document chunks
- Summaries are generated for processed documents
- Error cases return appropriate HTTP status codes
- Multiple clients can operate independently

## Troubleshooting Common Issues

**Service not responding**: Check that `docker-compose up -d` completed successfully and all containers are running.

**Authentication failures**: Make sure you're using a fresh JWT token and including the Authorization header correctly.

**Upload failures**: Verify file exists and is a supported format (PDF, TXT, DOCX, etc.).

**Search returns no results**: Wait for document processing to complete (check status endpoint) before searching.

**Processing stuck**: Check Docker logs for Ollama and PostgreSQL containers to ensure AI services are working.

These tests verify that our service provides real value to clients by enabling them to upload documents, have them processed with AI, and then search through them semantically - which is exactly what a knowledge management system should do.

## Additional Security Testing

### Authentication Security
**Token Tampering**: Verify that modified tokens are rejected
**Invalid Tokens**: Test with expired or malformed tokens

### Authorization
**User Isolation**: Ensure users can only access their own data
**Admin Operations**: Verify proper permission checks for admin functions

### Input Validation
**SQL Injection**: Test malicious input in search queries
**File Upload Security**: Attempt to upload malicious files
**XSS Prevention**: Test script injection in text fields

## Data Validation Testing

### File Upload Validation
**File Type Validation**: Verify only supported formats are accepted
**File Size Limits**: Test enforcement of maximum file size
**File Content Validation**: Check file integrity validation

### Search Query Validation
**Query Length Limits**: Test handling of very long queries
**Special Character Handling**: Test Unicode, symbols, etc.
**Encoding Issues**: Verify proper UTF-8 handling

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
**Small Text File**: Files under 1KB for quick tests
**Medium PDF**: 1-10MB typical documents
**Large Document**: Files near the 50MB limit
**Various Formats**: PDF, DOC, DOCX, TXT, HTML, PPT, etc.
**Edge Cases**: Empty files, corrupted files, unsupported formats

### Test Users
**Valid Test User**: For standard authentication testing
**Multiple Users**: For user isolation testing
**Invalid Credentials**: For negative testing scenarios

## Automated Testing Integration

### CI/CD Pipeline Tests
**Unit Tests**: Verify all service layer tests pass
**Integration Tests**: Test database connectivity
**API Tests**: Validate endpoint responses
**Security Tests**: Check authentication and authorization

### Test Coverage
**Code Coverage**: Maintain above 80% branch coverage
**API Coverage**: Ensure all endpoints are tested
**Error Path Coverage**: Test all error scenarios

## Test Results Documentation

### Test Execution Log
**Test Date/Time**: Record when tests were executed
**Environment**: Document service version and dependencies
**Test Results**: Track pass/fail status for each test case
**Issues Found**: Log bugs, performance issues, etc.
**Recommendations**: Note improvements and fixes needed

### Performance Metrics
**Response Times**: Measure average response time per endpoint
**Throughput**: Test requests per second capacity
**Resource Usage**: Monitor memory, CPU, and disk usage
**Error Rates**: Calculate percentage of failed requests

## Conclusion

This guide provides comprehensive coverage of all API endpoints and their expected behaviors. Execute these tests manually or integrate them into automated testing frameworks to ensure the AI Knowledge Management Service functions correctly across all scenarios.

**Total Test Cases**: 80+ individual test scenarios covering all endpoints, error conditions, and edge cases.