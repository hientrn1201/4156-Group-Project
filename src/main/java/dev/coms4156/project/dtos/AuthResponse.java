package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

  private String token;
  @Builder.Default
  private String type = "Bearer";
  private Long userId;
  private String username;
  private String role;
  private String message;
}
