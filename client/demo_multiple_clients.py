#!/usr/bin/env python3
"""
Multiple Client Demo Script

Demonstrates how multiple client instances can connect to the service simultaneously.
Each client is identified by a unique client ID and can perform operations concurrently.
"""

import threading
import time
import sys
import os
from client import KnowledgeServiceClient, load_env_file

def client_worker(client_id: str, service_url: str, operations: list, jwt_token: str = None):
    """
    Worker function for a single client instance.
    
    Args:
        client_id: Unique identifier for this client
        service_url: URL of the service
        operations: List of operations to perform
        jwt_token: JWT token for authentication
    """
    client = KnowledgeServiceClient(service_url, client_id, jwt_token)
    
    print(f"[{client_id}] Starting operations...")
    
    for operation in operations:
        try:
            # Test GET /api endpoint - welcome message
            if operation['type'] == 'welcome':
                result = client.get_welcome()
                print(f"[{client_id}] Welcome: {result.strip()}")
                
            # Test GET /api/v1/documents - list all documents
            elif operation['type'] == 'list':
                result = client.get_all_documents()
                count = result.get('count', 0)
                print(f"[{client_id}] Found {count} documents")
                
            # Test GET /api/v1/documents/stats - processing statistics
            elif operation['type'] == 'stats':
                result = client.get_statistics()
                total = result.get('total', 0)
                completion_rate = result.get('completionRate', 0.0)
                print(f"[{client_id}] Stats: {total} total docs, {completion_rate:.1%} completion rate")
                
            # Test GET /api/v1/search/{text} - semantic search
            elif operation['type'] == 'search':
                query = operation.get('query', 'test')
                result = client.search_documents(query)
                count = result.get('count', 0)
                print(f"[{client_id}] Search '{query}': {count} results")
                
            # Test POST /api/v1/documents - document upload
            elif operation['type'] == 'upload':
                file_path = operation.get('file_path')
                if file_path and os.path.exists(file_path):
                    result = client.upload_document(file_path)
                    doc_id = result.get('documentId', 'N/A')
                    print(f"[{client_id}] Uploaded {file_path} -> Document ID: {doc_id}")
                else:
                    print(f"[{client_id}] Skipping upload - file not found: {file_path}")
                    
            # Timing control for demonstration purposes
            elif operation['type'] == 'wait':
                duration = operation.get('duration', 1)
                print(f"[{client_id}] Waiting {duration}s...")
                time.sleep(duration)
                
        except Exception as e:
            print(f"[{client_id}] Error in {operation['type']}: {e}")
            
        # Small delay between operations
        time.sleep(0.5)
    
    print(f"[{client_id}] Completed all operations")

def main():
    """Main demo function."""
    # Load .env file automatically
    load_env_file()
    
    service_url = os.getenv('SERVICE_URL', 'http://localhost:8080')
    jwt_token = os.getenv('JWT_TOKEN')
    
    print("=== Multiple Client Demo ===")
    print(f"Service URL: {service_url}")
    print()
    
    # Check if service is available
    try:
        test_client = KnowledgeServiceClient(service_url, "test-client", jwt_token)
        test_client.get_welcome()
        print("✓ Service is available")
    except Exception as e:
        print(f"✗ Service is not available: {e}")
        print("Please make sure the service is running at", service_url)
        sys.exit(1)
    
    print()
    print("Starting multiple client instances...")
    print()
    
    # Define operations for each client - demonstrates concurrent API usage
    client_operations = {
        # Client focused on read operations
        'client-reader-1': [
            {'type': 'welcome'},
            {'type': 'list'},
            {'type': 'stats'},
            {'type': 'search', 'query': 'machine learning'},
            {'type': 'wait', 'duration': 2},
            {'type': 'list'},
        ],
        # Second client with different read patterns
        'client-reader-2': [
            {'type': 'wait', 'duration': 1},
            {'type': 'stats'},
            {'type': 'search', 'query': 'artificial intelligence'},
            {'type': 'list'},
            {'type': 'search', 'query': 'neural networks'},
        ],
        # Client that uploads documents
        'client-uploader-1': [
            {'type': 'welcome'},
            {'type': 'upload', 'file_path': '../test-document.txt'},
            {'type': 'wait', 'duration': 3},
            {'type': 'list'},
            {'type': 'stats'},
        ],
        # Client focused on search operations
        'client-searcher-1': [
            {'type': 'wait', 'duration': 2},
            {'type': 'search', 'query': 'test'},
            {'type': 'search', 'query': 'document'},
            {'type': 'search', 'query': 'knowledge'},
            {'type': 'stats'},
        ]
    }
    
    # Create and start threads for each client
    threads = []
    for client_id, operations in client_operations.items():
        thread = threading.Thread(
            target=client_worker,
            args=(client_id, service_url, operations, jwt_token),
            name=client_id
        )
        threads.append(thread)
        thread.start()
        
        # Stagger client starts slightly
        time.sleep(0.2)
    
    # Wait for all clients to complete
    for thread in threads:
        thread.join()
    
    print()
    print("=== Demo Complete ===")
    print("All client instances have finished their operations.")
    print("Check the service logs to see how each client was identified and tracked.")

if __name__ == '__main__':
    main()