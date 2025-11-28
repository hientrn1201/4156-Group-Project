#!/usr/bin/env python3
"""
Knowledge Management Service Client

A command-line client for interacting with the AI Knowledge Management Service.
This client demonstrates how to use the service API and can be run in multiple
instances simultaneously, each identified by a unique client ID.
"""

import argparse
import json
import os
import sys
import time
from typing import Optional, Dict, Any
import requests
from urllib.parse import quote


class KnowledgeServiceClient:
    """Client for interacting with the Knowledge Management Service API."""

    def __init__(self, base_url: str, client_id: str):
        """
        Initialize the client.

        Args:
            base_url: Base URL of the service (e.g., http://localhost:8080)
            client_id: Unique identifier for this client instance
        """
        self.base_url = base_url.rstrip('/')
        self.api_base = f"{self.base_url}/api/v1"
        self.client_id = client_id
        self.headers = {"X-Client-ID": client_id}

    def upload_document(self, file_path: str) -> Dict[str, Any]:
        """
        Upload a document for processing.

        Args:
            file_path: Path to the file to upload

        Returns:
            Response from the server
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"File not found: {file_path}")

        url = f"{self.api_base}/documents"
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'application/octet-stream')}
            response = requests.post(url, files=files, headers=self.headers, timeout=300)

        response.raise_for_status()
        return response.json()

    def get_all_documents(self, filename: Optional[str] = None) -> Dict[str, Any]:
        """
        Get all documents, optionally filtered by filename.

        Args:
            filename: Optional filename filter

        Returns:
            Response containing list of documents
        """
        url = f"{self.api_base}/documents"
        params = {}
        if filename:
            params['filename'] = filename

        response = requests.get(url, headers=self.headers, params=params)
        response.raise_for_status()
        return response.json()

    def get_document(self, document_id: int) -> Dict[str, Any]:
        """
        Get a specific document by ID.

        Args:
            document_id: Document ID

        Returns:
            Document details
        """
        url = f"{self.api_base}/documents/{document_id}"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get_document_summary(self, document_id: int) -> Dict[str, Any]:
        """
        Get the summary for a document.

        Args:
            document_id: Document ID

        Returns:
            Document summary
        """
        url = f"{self.api_base}/documents/{document_id}/summary"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get_documents_with_summaries(self) -> Dict[str, Any]:
        """
        Get all documents that have summaries.

        Returns:
            List of documents with summaries
        """
        url = f"{self.api_base}/documents/summaries"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get_statistics(self) -> Dict[str, Any]:
        """
        Get processing statistics for all documents.

        Returns:
            Processing statistics
        """
        url = f"{self.api_base}/documents/stats"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def search_documents(self, query: str) -> Dict[str, Any]:
        """
        Search documents using semantic search.

        Args:
            query: Search query text

        Returns:
            Search results
        """
        # URL encode the query
        encoded_query = quote(query)
        url = f"{self.api_base}/search/{encoded_query}"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def delete_document(self, document_id: int) -> Dict[str, Any]:
        """
        Delete a document by ID.

        Args:
            document_id: Document ID to delete

        Returns:
            Deletion confirmation
        """
        url = f"{self.api_base}/documents/{document_id}"
        response = requests.delete(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get_welcome(self) -> str:
        """
        Get the welcome message from the service.

        Returns:
            Welcome message
        """
        url = f"{self.base_url}/api"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.text


def print_json(data: Any, indent: int = 2):
    """Pretty print JSON data."""
    print(json.dumps(data, indent=indent, default=str))


def print_document(doc: Dict[str, Any]):
    """Print a document in a readable format."""
    print(f"\nDocument ID: {doc.get('id', 'N/A')}")
    print(f"Filename: {doc.get('filename', 'N/A')}")
    print(f"Content Type: {doc.get('contentType', 'N/A')}")
    print(f"File Size: {doc.get('fileSize', 'N/A')} bytes")
    print(f"Status: {doc.get('processingStatus', 'N/A')}")
    if doc.get('summary'):
        summary = doc.get('summary', '')
        if len(summary) > 200:
            print(f"Summary: {summary[:200]}...")
        else:
            print(f"Summary: {summary}")
    print(f"Uploaded: {doc.get('uploadedAt', 'N/A')}")


def print_search_results(results: Dict[str, Any]):
    """Print search results in a readable format."""
    query = results.get('query', '')
    count = results.get('count', 0)
    result_list = results.get('results', [])

    print(f"\nSearch Query: '{query}'")
    print(f"Found {count} result(s):\n")

    for i, result in enumerate(result_list, 1):
        print(f"Result {i}:")
        if isinstance(result, dict):
            chunk_id = result.get('id', 'N/A')
            text_content = result.get('textContent', '')
            document = result.get('document', {})
            doc_id = document.get('id', 'N/A') if isinstance(document, dict) else 'N/A'
            doc_filename = document.get('filename', 'N/A') if isinstance(document, dict) else 'N/A'

            print(f"  Chunk ID: {chunk_id}")
            print(f"  From Document: {doc_filename} (ID: {doc_id})")
            if text_content:
                preview = text_content[:150] + "..." if len(text_content) > 150 else text_content
                print(f"  Content: {preview}")
        print()


def print_statistics(stats: Dict[str, Any]):
    """Print statistics in a readable format."""
    total = stats.get('total', 0)
    by_status = stats.get('byStatus', {})
    completion_rate = stats.get('completionRate', 0.0)
    failure_rate = stats.get('failureRate', 0.0)

    print(f"\nProcessing Statistics:")
    print(f"  Total Documents: {total}")
    print(f"  Completion Rate: {completion_rate:.2%}")
    print(f"  Failure Rate: {failure_rate:.2%}")
    print(f"\n  Status Breakdown:")
    for status, count in by_status.items():
        print(f"    {status}: {count}")


def main():
    """Main entry point for the client."""
    parser = argparse.ArgumentParser(
        description='Knowledge Management Service Client',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Upload a document
  python client.py upload document.pdf

  # List all documents
  python client.py list

  # Get a specific document
  python client.py get 1

  # Search for documents
  python client.py search "machine learning"

  # Get document summary
  python client.py summary 1

  # Get statistics
  python client.py stats

  # Delete a document
  python client.py delete 1

  # Use a different service URL and client ID
  python client.py --url http://localhost:8080 --client-id my-client-001 upload doc.pdf
        """
    )

    parser.add_argument(
        '--url',
        default=os.getenv('SERVICE_URL', 'http://localhost:8080'),
        help='Base URL of the service (default: http://localhost:8080 or SERVICE_URL env var)'
    )
    parser.add_argument(
        '--client-id',
        default=os.getenv('CLIENT_ID', f'client-{int(time.time())}'),
        help='Unique client identifier (default: auto-generated or CLIENT_ID env var)'
    )

    subparsers = parser.add_subparsers(dest='command', help='Available commands')

    # Upload command
    upload_parser = subparsers.add_parser('upload', help='Upload a document')
    upload_parser.add_argument('file', help='Path to the file to upload')

    # List command
    list_parser = subparsers.add_parser('list', help='List all documents')
    list_parser.add_argument('--filename', help='Filter by filename')

    # Get command
    get_parser = subparsers.add_parser('get', help='Get a specific document')
    get_parser.add_argument('id', type=int, help='Document ID')

    # Summary command
    summary_parser = subparsers.add_parser('summary', help='Get document summary')
    summary_parser.add_argument('id', type=int, help='Document ID')

    # Summaries command
    subparsers.add_parser('summaries', help='Get all documents with summaries')

    # Stats command
    subparsers.add_parser('stats', help='Get processing statistics')

    # Search command
    search_parser = subparsers.add_parser('search', help='Search documents')
    search_parser.add_argument('query', help='Search query')

    # Delete command
    delete_parser = subparsers.add_parser('delete', help='Delete a document')
    delete_parser.add_argument('id', type=int, help='Document ID')

    # Welcome command
    subparsers.add_parser('welcome', help='Get welcome message')

    args = parser.parse_args()

    if not args.command:
        parser.print_help()
        sys.exit(1)

    # Create client
    client = KnowledgeServiceClient(args.url, args.client_id)

    try:
        if args.command == 'upload':
            print(f"Uploading {args.file}...")
            result = client.upload_document(args.file)
            print("\n✓ Upload successful!")
            print_json(result)

        elif args.command == 'list':
            result = client.get_all_documents(args.filename if hasattr(args, 'filename') else None)
            documents = result.get('documents', [])
            count = result.get('count', 0)
            print(f"\nFound {count} document(s):")
            for doc in documents:
                print_document(doc)

        elif args.command == 'get':
            result = client.get_document(args.id)
            print_document(result)

        elif args.command == 'summary':
            result = client.get_document_summary(args.id)
            print(f"\nDocument ID: {result.get('documentId', 'N/A')}")
            print(f"Summary:\n{result.get('summary', 'N/A')}")

        elif args.command == 'summaries':
            result = client.get_documents_with_summaries()
            documents = result.get('documents', [])
            count = result.get('count', 0)
            print(f"\nFound {count} document(s) with summaries:")
            for doc in documents:
                print_document(doc)

        elif args.command == 'stats':
            result = client.get_statistics()
            print_statistics(result)

        elif args.command == 'search':
            print(f"Searching for '{args.query}'...")
            result = client.search_documents(args.query)
            print_search_results(result)

        elif args.command == 'delete':
            result = client.delete_document(args.id)
            print(f"\n✓ {result.get('message', 'Document deleted')}")
            print_json(result)

        elif args.command == 'welcome':
            message = client.get_welcome()
            print(message)

    except requests.exceptions.HTTPError as e:
        print(f"\n✗ HTTP Error: {e}", file=sys.stderr)
        if e.response is not None:
            try:
                error_detail = e.response.json()
                print(f"  Details: {json.dumps(error_detail, indent=2)}", file=sys.stderr)
            except:
                print(f"  Response: {e.response.text}", file=sys.stderr)
        sys.exit(1)

    except requests.exceptions.ConnectionError:
        print(f"\n✗ Error: Could not connect to service at {args.url}", file=sys.stderr)
        print("  Make sure the service is running and the URL is correct.", file=sys.stderr)
        sys.exit(1)

    except requests.exceptions.Timeout:
        print(f"\n✗ Error: Request timed out", file=sys.stderr)
        sys.exit(1)

    except FileNotFoundError as e:
        print(f"\n✗ Error: {e}", file=sys.stderr)
        sys.exit(1)

    except Exception as e:
        print(f"\n✗ Unexpected error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()

