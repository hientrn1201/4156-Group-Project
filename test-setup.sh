#!/bin/bash

# RAG Implementation Test Script
echo "Testing RAG Implementation with Spring AI, Ollama, and PGVector"
echo "=============================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is running"

# Check if services are running
if ! docker ps | grep -q "ollama"; then
    echo "❌ Ollama container is not running. Starting services..."
    docker-compose up -d
    echo "⏳ Waiting for services to start..."
    sleep 30
else
    echo "✅ Ollama container is running"
fi

if ! docker ps | grep -q "postgres-pgvector"; then
    echo "❌ PostgreSQL container is not running"
    exit 1
else
    echo "✅ PostgreSQL container is running"
fi

# Check if llama3.2 model is available
echo "🔍 Checking if llama3.2 model is available..."
if docker exec ollama ollama list | grep -q "llama3.2"; then
    echo "✅ llama3.2 model is available"
else
    echo "⏳ Pulling llama3.2 model (this may take a few minutes)..."
    docker exec ollama ollama pull llama3.2
    echo "✅ llama3.2 model downloaded"
fi

# Test Ollama API
echo "🔍 Testing Ollama API..."
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "✅ Ollama API is responding"
else
    echo "❌ Ollama API is not responding"
    exit 1
fi

# Test PostgreSQL connection
echo "🔍 Testing PostgreSQL connection..."
if docker exec postgres-pgvector pg_isready -U postgres > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready"
else
    echo "❌ PostgreSQL is not ready"
    exit 1
fi

# Check if PGVector extension is installed
echo "🔍 Checking PGVector extension..."
if docker exec postgres-pgvector psql -U postgres -d knowledge_db -c "SELECT * FROM pg_extension WHERE extname = 'vector';" | grep -q "vector"; then
    echo "✅ PGVector extension is installed"
else
    echo "⏳ Installing PGVector extension..."
    docker exec postgres-pgvector psql -U postgres -d knowledge_db -c "CREATE EXTENSION IF NOT EXISTS vector;"
    echo "✅ PGVector extension installed"
fi

echo ""
echo "🎉 All services are ready!"
echo ""
echo "Next steps:"
echo "1. Build the project: mvn clean compile"
echo "2. Run the application: mvn spring-boot:run"
echo "3. Test the RAG API endpoints (see RAG_IMPLEMENTATION.md)"
echo ""
echo "Useful commands:"
echo "- View logs: docker-compose logs -f"
echo "- Stop services: docker-compose down"
echo "- Restart services: docker-compose restart"
