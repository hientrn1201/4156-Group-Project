package dev.coms4156.project.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DocumentListResponse {
    private List<DocumentDTO> documents;

    private Long count;

    private String messages;
}