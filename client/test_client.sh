#!/bin/bash
# Simple test script for the client

echo "=== Testing Knowledge Management Service Client ==="
echo ""

# Check if service is running
echo "1. Checking if service is running..."
if curl -s http://localhost:8080/api > /dev/null 2>&1; then
    echo "   ✓ Service is running"
else
    echo "   ✗ Service is NOT running"
    echo "   Please start your service first:"
    echo "   - Start Docker: docker-compose up -d"
    echo "   - Start service: mvn spring-boot:run"
    echo ""
    exit 1
fi

echo ""
echo "2. Testing welcome endpoint..."
python3 client/client.py welcome

echo ""
echo "3. Testing get all documents..."
python3 client/client.py list

echo ""
echo "4. Testing get statistics..."
python3 client/client.py stats

echo ""
echo "=== Basic tests complete ==="
echo ""
echo "To test more features:"
echo "  - Upload: python3 client/client.py upload <file>"
echo "  - Search: python3 client/client.py search \"<query>\""
echo "  - Get doc: python3 client/client.py get <id>"

