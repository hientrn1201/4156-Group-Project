package dev.coms4156.project.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentSearchResponse {
  private String query;

  private List<DocumentChunkDTO> results;

  private Integer count;

  private String message;
}
