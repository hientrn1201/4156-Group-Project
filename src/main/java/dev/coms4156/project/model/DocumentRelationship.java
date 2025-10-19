package dev.coms4156.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_relationships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_chunk_id", nullable = false)
    private DocumentChunk sourceChunk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_chunk_id", nullable = false)
    private DocumentChunk targetChunk;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum RelationshipType {
        SEMANTIC_SIMILARITY,
        TOPICAL_RELATEDNESS,
        TEMPORAL_SEQUENCE,
        CAUSAL_RELATIONSHIP,
        REFERENCE,
        CONTRAST,
        EXAMPLE,
        DEFINITION
    }
}
