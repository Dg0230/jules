package com.example.authservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;

    // User details (populated for response)
    private Long userId;
    private String username;

    // ReviewCard details (populated for response)
    private Long reviewCardId;
    private String reviewCardName; // Potentially name or cardIdentifier of the ReviewCard

    // Merchant details (populated for response)
    private Long merchantId;
    private String merchantName;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 10000, message = "Text content must be less than 10000 characters")
    private String textContent;

    private List<@URL(message = "Each image URL must be valid") @Size(max=2048) String> imageUrls = new ArrayList<>();

    private List<@URL(message = "Each video URL must be valid") @Size(max=2048) String> videoUrls = new ArrayList<>();

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., pending_approval, approved, rejected

    @Size(max = 50, message = "Platform must be less than 50 characters")
    private String platform;

    @Size(max = 255, message = "Platform Review ID must be less than 255 characters")
    private String platformReviewId;

    private OffsetDateTime sharedAt;

    // Timestamps (populated for response)
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
