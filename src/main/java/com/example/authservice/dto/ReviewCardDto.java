package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCardDto {

    private Long id;

    @NotNull(message = "Merchant ID is required")
    private Long merchantId;
    private String merchantName; // Read-only, populated by service

    @NotBlank(message = "Review card name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 255, message = "Card identifier must be less than 255 characters")
    private String cardIdentifier; // From schema, unique business key

    @Size(max = 2048, message = "QR code URL must be less than 2048 characters")
    private String qrCodeUrl;

    @Size(max = 255, message = "NFC tag ID must be less than 255 characters")
    private String nfcTagId;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., active, inactive, assigned

    private String aiPromptCustom; // Can be long, @Lob in entity
    
    private String notes; // Can be long, @Lob in entity

    // For request: list of material collection IDs to associate
    private Set<Long> collectionIds = new HashSet<>();

    // For response: list of full MaterialCollectionDto objects
    private Set<MaterialCollectionDto> materialCollections = new HashSet<>();

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
