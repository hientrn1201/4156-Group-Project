package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for error messages.
 * Used to return error information to API clients.
 */
@Data
@AllArgsConstructor
@Builder
public class ErrorResponse {
  private String error;
}
