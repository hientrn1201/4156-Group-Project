#!/usr/bin/env python3
"""
End-to-End Test Runner

Executes comprehensive API testing based on the E2E_TESTING_CHECKLIST.md
Tests all endpoints with various scenarios and documents results.
"""

import json
import os
import sys
import time
import requests
from typing import Dict, List, Any, Optional
from client import KnowledgeServiceClient, load_env_file

class E2ETestRunner:
    """Comprehensive end-to-end test runner for the Knowledge Management Service."""
    
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.api_base = f"{base_url}/api/v1"
        self.test_results = []
        self.jwt_token = None
        self.test_document_id = None
        
    def log_test(self, test_name: str, status: str, details: str = "", response_data: Any = None):
        """Log test result."""
        result = {
            "test_name": test_name,
            "status": status,  # PASS, FAIL, SKIP
            "details": details,
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
            "response_data": response_data
        }
        self.test_results.append(result)
        
        status_symbol = "[PASS]" if status == "PASS" else "[FAIL]" if status == "FAIL" else "[SKIP]"
        print(f"{status_symbol} {test_name}: {status}")
        if details:
            print(f"   {details}")
    
    def test_service_health(self):
        """Test basic service health."""
        try:
            response = requests.get(f"{self.base_url}/api", timeout=10)
            if response.status_code == 200 and "Welcome" in response.text:
                self.log_test("Service Health Check", "PASS", f"Service responding: {response.text.strip()}")
                return True
            elif response.status_code == 401:
                # Service is running but requires auth - that's fine for health check
                self.log_test("Service Health Check", "PASS", "Service is running (requires authentication)")
                return True
            else:
                self.log_test("Service Health Check", "FAIL", f"Unexpected response: {response.status_code}")
                return False
        except Exception as e:
            self.log_test("Service Health Check", "FAIL", f"Connection failed: {str(e)}")
            return False
    
    def test_authentication(self):
        """Test authentication endpoints."""
        # Test user registration
        test_user = {
            "username": f"testuser_{int(time.time())}",
            "email": f"test_{int(time.time())}@example.com",
            "password": "password123"
        }
        
        try:
            # Register new user
            response = requests.post(
                f"{self.api_base}/auth/register",
                json=test_user,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "token" in data:
                    self.jwt_token = data["token"]
                    self.log_test("User Registration", "PASS", f"User registered: {test_user['username']}")
                else:
                    self.log_test("User Registration", "FAIL", "No token in response")
                    return False
            else:
                self.log_test("User Registration", "FAIL", f"Status: {response.status_code}, Response: {response.text}")
                return False
            
            # Test duplicate registration
            response = requests.post(
                f"{self.api_base}/auth/register",
                json=test_user,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 400:
                self.log_test("Duplicate Registration Prevention", "PASS", "Duplicate user rejected")
            else:
                self.log_test("Duplicate Registration Prevention", "FAIL", f"Expected 400, got {response.status_code}")
            
            # Test login
            login_data = {"username": test_user["username"], "password": test_user["password"]}
            response = requests.post(
                f"{self.api_base}/auth/login",
                json=login_data,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "token" in data:
                    self.log_test("User Login", "PASS", "Login successful")
                else:
                    self.log_test("User Login", "FAIL", "No token in login response")
            else:
                self.log_test("User Login", "FAIL", f"Status: {response.status_code}")
            
            # Test invalid login
            invalid_login = {"username": test_user["username"], "password": "wrongpassword"}
            response = requests.post(
                f"{self.api_base}/auth/login",
                json=invalid_login,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 401:
                self.log_test("Invalid Login Rejection", "PASS", "Invalid credentials rejected")
            else:
                self.log_test("Invalid Login Rejection", "FAIL", f"Expected 401, got {response.status_code}")
            
            return True
            
        except Exception as e:
            self.log_test("Authentication Tests", "FAIL", f"Exception: {str(e)}")
            return False
    
    def get_auth_headers(self):
        """Get headers with JWT token."""
        if self.jwt_token:
            return {"Authorization": f"Bearer {self.jwt_token}"}
        return {}
    
    def test_document_operations(self):
        """Test document management endpoints."""
        headers = self.get_auth_headers()
        
        # Test getting documents (empty state)
        try:
            response = requests.get(f"{self.api_base}/documents", headers=headers)
            if response.status_code == 200:
                data = response.json()
                self.log_test("Get All Documents (Empty)", "PASS", f"Found {data.get('count', 0)} documents")
            else:
                self.log_test("Get All Documents (Empty)", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Get All Documents (Empty)", "FAIL", f"Exception: {str(e)}")
        
        # Test document upload
        test_file_content = "This is a test document for end-to-end testing.\nIt contains sample text for processing."
        test_file_path = "/tmp/e2e_test_document.txt"
        
        try:
            with open(test_file_path, 'w') as f:
                f.write(test_file_content)
            
            with open(test_file_path, 'rb') as f:
                files = {'file': ('e2e_test_document.txt', f, 'text/plain')}
                response = requests.post(f"{self.api_base}/documents", files=files, headers=headers, timeout=60)
            
            if response.status_code == 200:
                data = response.json()
                self.test_document_id = data.get('documentId')
                self.log_test("Document Upload", "PASS", f"Document uploaded with ID: {self.test_document_id}")
            else:
                self.log_test("Document Upload", "FAIL", f"Status: {response.status_code}, Response: {response.text}")
                return False
            
            # Clean up test file
            os.remove(test_file_path)
            
        except Exception as e:
            self.log_test("Document Upload", "FAIL", f"Exception: {str(e)}")
            return False
        
        # Test getting documents (with data)
        try:
            response = requests.get(f"{self.api_base}/documents", headers=headers)
            if response.status_code == 200:
                data = response.json()
                count = data.get('count', 0)
                if count > 0:
                    self.log_test("Get All Documents (With Data)", "PASS", f"Found {count} documents")
                else:
                    self.log_test("Get All Documents (With Data)", "FAIL", "No documents found after upload")
            else:
                self.log_test("Get All Documents (With Data)", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Get All Documents (With Data)", "FAIL", f"Exception: {str(e)}")
        
        # Test getting specific document
        if self.test_document_id:
            try:
                response = requests.get(f"{self.api_base}/documents/{self.test_document_id}", headers=headers)
                if response.status_code == 200:
                    data = response.json()
                    self.log_test("Get Document by ID", "PASS", f"Retrieved document: {data.get('filename')}")
                else:
                    self.log_test("Get Document by ID", "FAIL", f"Status: {response.status_code}")
            except Exception as e:
                self.log_test("Get Document by ID", "FAIL", f"Exception: {str(e)}")
        
        # Test getting non-existent document
        try:
            response = requests.get(f"{self.api_base}/documents/99999", headers=headers)
            if response.status_code == 404:
                self.log_test("Get Non-existent Document", "PASS", "404 returned for non-existent document")
            else:
                self.log_test("Get Non-existent Document", "FAIL", f"Expected 404, got {response.status_code}")
        except Exception as e:
            self.log_test("Get Non-existent Document", "FAIL", f"Exception: {str(e)}")
        
        return True
    
    def test_search_operations(self):
        """Test search functionality."""
        headers = self.get_auth_headers()
        
        # Test basic search
        try:
            response = requests.get(f"{self.api_base}/search/test", headers=headers)
            if response.status_code == 200:
                data = response.json()
                self.log_test("Basic Search", "PASS", f"Search returned {data.get('count', 0)} results")
            else:
                self.log_test("Basic Search", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Basic Search", "FAIL", f"Exception: {str(e)}")
        
        # Test search with special characters
        try:
            import urllib.parse
            query = urllib.parse.quote("test document")
            response = requests.get(f"{self.api_base}/search/{query}", headers=headers)
            if response.status_code == 200:
                data = response.json()
                self.log_test("Search with Spaces", "PASS", f"Search with spaces returned {data.get('count', 0)} results")
            else:
                self.log_test("Search with Spaces", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Search with Spaces", "FAIL", f"Exception: {str(e)}")
    
    def test_summary_operations(self):
        """Test summary functionality."""
        headers = self.get_auth_headers()
        
        # Test getting documents with summaries
        try:
            response = requests.get(f"{self.api_base}/documents/summaries", headers=headers)
            if response.status_code == 200:
                data = response.json()
                self.log_test("Get Documents with Summaries", "PASS", f"Found {data.get('count', 0)} documents with summaries")
            else:
                self.log_test("Get Documents with Summaries", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Get Documents with Summaries", "FAIL", f"Exception: {str(e)}")
        
        # Test getting specific document summary
        if self.test_document_id:
            try:
                response = requests.get(f"{self.api_base}/documents/{self.test_document_id}/summary", headers=headers)
                if response.status_code == 200:
                    data = response.json()
                    summary = data.get('summary', '')
                    self.log_test("Get Document Summary", "PASS", f"Summary length: {len(summary)} chars")
                elif response.status_code == 404:
                    self.log_test("Get Document Summary", "PASS", "Summary not yet generated (404 expected)")
                else:
                    self.log_test("Get Document Summary", "FAIL", f"Status: {response.status_code}")
            except Exception as e:
                self.log_test("Get Document Summary", "FAIL", f"Exception: {str(e)}")
    
    def test_statistics(self):
        """Test statistics endpoint."""
        headers = self.get_auth_headers()
        
        try:
            response = requests.get(f"{self.api_base}/documents/stats", headers=headers)
            if response.status_code == 200:
                data = response.json()
                total = data.get('total', 0)
                completion_rate = data.get('completionRate', 0)
                self.log_test("Get Statistics", "PASS", f"Total: {total}, Completion Rate: {completion_rate:.2%}")
            else:
                self.log_test("Get Statistics", "FAIL", f"Status: {response.status_code}")
        except Exception as e:
            self.log_test("Get Statistics", "FAIL", f"Exception: {str(e)}")
    
    def test_relationships(self):
        """Test relationship endpoints."""
        headers = self.get_auth_headers()
        
        if self.test_document_id:
            try:
                response = requests.get(f"{self.api_base}/relationships/{self.test_document_id}", headers=headers)
                if response.status_code == 200:
                    data = response.json()
                    count = data.get('count', 0)
                    self.log_test("Get Document Relationships", "PASS", f"Found {count} relationships")
                else:
                    self.log_test("Get Document Relationships", "FAIL", f"Status: {response.status_code}")
            except Exception as e:
                self.log_test("Get Document Relationships", "FAIL", f"Exception: {str(e)}")
    
    def test_unauthorized_access(self):
        """Test endpoints without authentication."""
        # Test protected endpoint without token
        try:
            response = requests.get(f"{self.api_base}/documents")
            if response.status_code == 401:
                self.log_test("Unauthorized Access Prevention", "PASS", "Protected endpoint requires authentication")
            else:
                self.log_test("Unauthorized Access Prevention", "FAIL", f"Expected 401, got {response.status_code}")
        except Exception as e:
            self.log_test("Unauthorized Access Prevention", "FAIL", f"Exception: {str(e)}")
    
    def test_cleanup(self):
        """Clean up test data."""
        headers = self.get_auth_headers()
        
        if self.test_document_id:
            try:
                response = requests.delete(f"{self.api_base}/documents/{self.test_document_id}", headers=headers)
                if response.status_code == 200:
                    self.log_test("Document Deletion", "PASS", f"Test document {self.test_document_id} deleted")
                else:
                    self.log_test("Document Deletion", "FAIL", f"Status: {response.status_code}")
            except Exception as e:
                self.log_test("Document Deletion", "FAIL", f"Exception: {str(e)}")
    
    def run_all_tests(self):
        """Run comprehensive test suite."""
        print("Starting End-to-End Test Suite")
        print("=" * 50)
        
        # Service health check
        if not self.test_service_health():
            print("Service health check failed. Aborting tests.")
            return False
        
        # Authentication tests
        if not self.test_authentication():
            print("Authentication tests failed. Aborting tests.")
            return False
        
        # Test unauthorized access
        self.test_unauthorized_access()
        
        # Document operations
        self.test_document_operations()
        
        # Search operations
        self.test_search_operations()
        
        # Summary operations
        self.test_summary_operations()
        
        # Statistics
        self.test_statistics()
        
        # Relationships
        self.test_relationships()
        
        # Cleanup
        self.test_cleanup()
        
        # Summary
        self.print_test_summary()
        
        return True
    
    def print_test_summary(self):
        """Print test execution summary."""
        print("\n" + "=" * 50)
        print("Test Execution Summary")
        print("=" * 50)
        
        total_tests = len(self.test_results)
        passed_tests = len([r for r in self.test_results if r["status"] == "PASS"])
        failed_tests = len([r for r in self.test_results if r["status"] == "FAIL"])
        skipped_tests = len([r for r in self.test_results if r["status"] == "SKIP"])
        
        print(f"Total Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Skipped: {skipped_tests}")
        print(f"Success Rate: {(passed_tests/total_tests)*100:.1f}%")
        
        if failed_tests > 0:
            print("\nFailed Tests:")
            for result in self.test_results:
                if result["status"] == "FAIL":
                    print(f"  - {result['test_name']}: {result['details']}")
        
        # Save detailed results
        results_file = "e2e_test_results.json"
        with open(results_file, 'w') as f:
            json.dump(self.test_results, f, indent=2, default=str)
        print(f"\nDetailed results saved to: {results_file}")

def main():
    """Main entry point."""
    load_env_file()
    
    service_url = os.getenv('SERVICE_URL', 'http://localhost:8080')
    
    print(f"Testing service at: {service_url}")
    
    runner = E2ETestRunner(service_url)
    success = runner.run_all_tests()
    
    sys.exit(0 if success else 1)

if __name__ == '__main__':
    main()