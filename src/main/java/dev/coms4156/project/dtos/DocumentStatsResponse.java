package dev.coms4156.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentStatsResponse {
    private Long total;

    private DocumentStatusCounts byStatus;

    private Double completionRate;

    private Double failureRate;
}