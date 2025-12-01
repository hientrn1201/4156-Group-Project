package dev.coms4156.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity representing relationship between documents.
 */
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

  @ColumnTransformer(write = "?::jsonb")
  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * RelationshipType Enum.
   */
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
