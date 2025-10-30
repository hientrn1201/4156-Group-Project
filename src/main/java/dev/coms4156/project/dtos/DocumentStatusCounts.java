package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentStatusCounts {

    private Long uploaded;

    private Long textExtracted;

    private Long chunked;

    private Long embeddingsGenerated;

    private Long summarized;

    private Long completed;

    private Long failed;

}