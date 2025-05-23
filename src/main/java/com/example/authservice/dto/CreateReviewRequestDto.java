package com.example.authservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateReviewRequestDto {

    @NotNull(message = "Review Card ID is required")
    private Long reviewCardId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 10000, message = "Text content must be less than 10000 characters")
    private String textContent;

    private List<@URL(message = "Each image URL must be valid") @Size(max=2048) String> imageUrls = new ArrayList<>();

    private List<@URL(message = "Each video URL must be valid") @Size(max=2048) String> videoUrls = new ArrayList<>();

    @Size(max = 50, message = "Platform must be less than 50 characters")
    private String platform;

    @Size(max = 255, message = "Platform Review ID must be less than 255 characters")
    private String platformReviewId;

    private OffsetDateTime sharedAt;
}
