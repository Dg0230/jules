package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Added as it's a common field, was in schema.sql

    @Column(length = 50)
    private String type; // e.g., image, video, text

    @Column(name = "url", length = 2048) // Assuming 'content' from schema.sql might map to url
    private String url;

    @Lob // For potentially long text
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id") // Foreign key in materials table
    private User uploader;

    @Column(length = 50)
    private String status = "pending"; // e.g., pending, approved, rejected

    @Column(name = "usage_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long usageCount = 0L;

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastUsedAt;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @ManyToMany(mappedBy = "materials", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<MaterialCollection> collections = new HashSet<>();
}
