package dev.coms4156.project.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void testConstructorAndGetters() {
    String errorMessage = "Test error message";
    ErrorResponse errorResponse = new ErrorResponse(errorMessage);

    assertEquals(errorMessage, errorResponse.getError());
  }

  @Test
  void testSetters() {
    ErrorResponse errorResponse = new ErrorResponse("Initial message");
    String newMessage = "Updated error message";
    
    errorResponse.setError(newMessage);
    
    assertEquals(newMessage, errorResponse.getError());
  }

  @Test
  void testEqualsAndHashCode() {
    ErrorResponse response1 = new ErrorResponse("Same message");
    ErrorResponse response2 = new ErrorResponse("Same message");
    ErrorResponse response3 = new ErrorResponse("Different message");

    assertEquals(response1, response2);
    assertEquals(response1.hashCode(), response2.hashCode());
    assertNotEquals(response1, response3);
    assertNotEquals(response1.hashCode(), response3.hashCode());
  }

  @Test
  void testToString() {
    ErrorResponse errorResponse = new ErrorResponse("Test message");
    String toString = errorResponse.toString();
    
    assertNotNull(toString);
  }
}