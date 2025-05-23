package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "material_collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id") // Foreign key in material_collections table
    private User creator;

    @Column(name = "theme_or_room_id")
    private String themeOrRoomId; // Can be a specific ID or a theme name

    @Column
    private String season; // e.g., Spring 2024, Q1

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE }) // Owning side
    @JoinTable(
            name = "collection_materials", // Join table name from schema
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private Set<Material> materials = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Convenience methods for managing materials
    public void addMaterial(Material material) {
        this.materials.add(material);
        material.getCollections().add(this);
    }

    public void removeMaterial(Material material) {
        this.materials.remove(material);
        material.getCollections().remove(this);
    }
}
