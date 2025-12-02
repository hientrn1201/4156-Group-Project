package dev.coms4156.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing a chunk of text from a document with embedding vector.
 */
@Entity
@Table(name = "document_chunks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  @JsonIgnore
  private Document document;

  @Column(name = "chunk_index", nullable = false)
  private Integer chunkIndex;

  @Column(name = "document_id", insertable = false, updatable = false)
  private Long documentId;

  @Column(name = "text_content", columnDefinition = "TEXT", nullable = false)
  private String textContent;

  @Column(name = "chunk_size")
  private Integer chunkSize;

  @Column(name = "start_position")
  private Integer startPosition;

  @Column(name = "end_position")
  private Integer endPosition;

  @JdbcTypeCode(SqlTypes.VECTOR)
  @Column(name = "embedding", columnDefinition = "vector(3072)")
  @Type(JsonType.class)
  @JsonIgnore
  private float[] embedding;

  @ColumnTransformer(write = "?::jsonb")
  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "sourceChunk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<DocumentRelationship> sourceRelationships;

  @OneToMany(mappedBy = "targetChunk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<DocumentRelationship> targetRelationships;
}
