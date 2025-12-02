#!/bin/bash

# End-to-End Test Execution Script
# This script runs comprehensive tests against the Knowledge Management Service API

echo "Knowledge Management Service - End-to-End Testing"
echo "=================================================="

# Check if service is running
echo "Checking service health..."
if ! curl -s http://localhost:8080/api > /dev/null 2>&1; then
    echo "ERROR: Service is not running at http://localhost:8080"
    echo "Please start the service first:"
    echo "  1. Start Docker services: docker-compose up -d"
    echo "  2. Start application: mvn spring-boot:run"
    exit 1
fi

echo "SUCCESS: Service is running"

# Check if Python client is available
if [ ! -f "client/client.py" ]; then
    echo "ERROR: Python client not found at client/client.py"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Check if .env file exists
if [ ! -f "client/.env" ]; then
    echo "WARNING: No .env file found. Creating one..."
    cp client/.env.example client/.env
    echo "Please edit client/.env and add your JWT token, then run this script again"
    echo "   Get a token by running:"
    echo "   curl -X POST http://localhost:8080/api/v1/auth/register \\"
    echo "     -H \"Content-Type: application/json\" \\"
    echo "     -d '{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password123\"}'"
    exit 1
fi

echo ""
echo "Running End-to-End Tests"
echo "========================"

# Run the comprehensive test runner
echo "Executing comprehensive test suite..."
cd client
python3 e2e_test_runner.py

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "SUCCESS: End-to-End tests completed successfully!"
else
    echo ""
    echo "ERROR: Some tests failed. Check the output above for details."
fi

echo ""
echo "Additional Manual Tests Available:"
echo "  - Basic client test: ./client/test_client.sh"
echo "  - Multi-client demo: python3 client/demo_multiple_clients.py"
echo "  - Individual commands: python3 client/client.py --help"

echo ""
echo "Test documentation available in:"
echo "  - E2E_TESTING_CHECKLIST.md (comprehensive test checklist)"
echo "  - client/e2e_test_results.json (detailed test results)"