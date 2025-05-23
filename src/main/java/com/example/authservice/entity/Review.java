package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Can be null if anonymous reviews allowed by schema, but usually not for user-submitted
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_card_id", nullable = false)
    private ReviewCard reviewCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false) // Denormalized, ensure this is consistent with reviewCard.merchant
    private Merchant merchant;

    @Column(nullable = false)
    private Integer rating; // e.g., 1-5

    @Lob
    @Column(name = "text_content")
    private String textContent; // Maps to 'comment' from original schema

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_image_urls", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url", length = 2048)
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_video_urls", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "video_url", length = 2048)
    private List<String> videoUrls = new ArrayList<>();

    @Column(length = 50, nullable = false)
    private String status = "pending_approval"; // e.g., pending_approval, approved, rejected

    @Column(length = 100)
    private String platform; // e.g., "Dianping", "Meituan", "Internal"

    @Column(name = "platform_review_id", length = 255)
    private String platformReviewId; // ID of the review on the external platform

    @Column(name = "shared_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime sharedAt; // Timestamp when the review was shared or posted

    @Column(name = "reviewer_ip_address", length = 100) // From original schema
    private String reviewerIpAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
}
