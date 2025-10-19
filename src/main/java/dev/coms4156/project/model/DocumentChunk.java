package dev.coms4156.project.model;

import dev.coms4156.project.converter.FloatArrayToPgVectorConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "text_content", columnDefinition = "TEXT", nullable = false)
    private String textContent;

    @Column(name = "chunk_size")
    private Integer chunkSize;

    @Column(name = "start_position")
    private Integer startPosition;

    @Column(name = "end_position")
    private Integer endPosition;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    @Convert(converter = FloatArrayToPgVectorConverter.class)
    private float[] embedding;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sourceChunk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentRelationship> sourceRelationships;

    @OneToMany(mappedBy = "targetChunk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentRelationship> targetRelationships;
}
