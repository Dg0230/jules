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
@Table(name = "review_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // As per task description

    @Column(name = "card_identifier", unique = true) // From schema.sql, often a unique business key
    private String cardIdentifier;

    @Column(name = "qr_code_url", length = 2048)
    private String qrCodeUrl;

    @Column(name = "nfc_tag_id", length = 255)
    private String nfcTagId;

    @Column(length = 50, nullable = false)
    private String status = "inactive"; // e.g., active, inactive, assigned

    @Lob
    @Column(name = "ai_prompt_custom")
    private String aiPromptCustom;
    
    @Lob // Added 'notes' from schema.sql as it's useful
    private String notes;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false) // A review card must belong to a merchant
    private Merchant merchant;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "review_card_material_collections",
            joinColumns = @JoinColumn(name = "review_card_id"),
            inverseJoinColumns = @JoinColumn(name = "material_collection_id") // Corrected based on typical schema naming
    )
    private Set<MaterialCollection> materialCollections = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Convenience methods for managing material collections
    public void addMaterialCollection(MaterialCollection collection) {
        this.materialCollections.add(collection);
        // Note: MaterialCollection does not have a direct back-reference to ReviewCard in this model.
        // If MaterialCollection also had a Set<ReviewCard>, we would add to it here:
        // collection.getReviewCards().add(this);
    }

    public void removeMaterialCollection(MaterialCollection collection) {
        this.materialCollections.remove(collection);
        // if (collection != null) {
        //    collection.getReviewCards().remove(this);
        // }
    }
}
