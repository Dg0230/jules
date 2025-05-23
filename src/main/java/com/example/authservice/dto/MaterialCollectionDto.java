package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class MaterialCollectionDto {

    private Long id;

    @NotBlank(message = "Collection name is required")
    @Size(max = 255, message = "Collection name must be less than 255 characters")
    private String name;

    private String description; // Can be long

    private Long creatorId; // Set by service during creation
    private String creatorUsername; // Read-only, populated by service for responses

    @Size(max = 255, message = "Theme/Room ID must be less than 255 characters")
    private String themeOrRoomId;

    @Size(max = 100, message = "Season must be less than 100 characters")
    private String season;

    // For request: list of material IDs to associate with the collection
    private Set<Long> materialIds = new HashSet<>();

    // For response: list of full MaterialDto objects
    private Set<MaterialDto> materials = new HashSet<>();

    private OffsetDateTime createdAt; // Read-only

    private OffsetDateTime updatedAt; // Read-only
}
