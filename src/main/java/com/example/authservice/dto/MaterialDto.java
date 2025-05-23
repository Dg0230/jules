package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL; // For URL validation

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {

    private Long id;

    @NotBlank(message = "Material name is required")
    @Size(max = 255, message = "Material name must be less than 255 characters")
    private String name;

    @Size(max = 50, message = "Type must be less than 50 characters")
    private String type; // e.g., image, video, text

    @URL(message = "URL must be valid")
    @Size(max = 2048, message = "URL must be less than 2048 characters")
    private String url;

    private String description; // Can be long, @Lob in entity

    private Long uploaderId; // Set during creation by service
    private String uploaderUsername; // Read-only, populated by service for responses

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., pending, approved, rejected

    private Long usageCount = 0L;

    private OffsetDateTime lastUsedAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime createdAt; // Read-only, populated by service

    private OffsetDateTime updatedAt; // Read-only, populated by service
}
