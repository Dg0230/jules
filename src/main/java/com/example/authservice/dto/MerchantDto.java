package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDto {

    private Long id; // Include for responses

    @NotBlank(message = "Merchant name is required")
    @Size(max = 255, message = "Merchant name must be less than 255 characters")
    private String name;

    @Size(max = 255, message = "Contact person name must be less than 255 characters")
    private String contactPerson;

    @Size(max = 50, message = "Contact phone must be less than 50 characters")
    private String contactPhone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @Size(max = 255, message = "Business license ID must be less than 255 characters")
    private String businessLicenseId;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., pending, active, inactive

    @Size(max = 1024, message = "Logo URL must be less than 1024 characters")
    private String logoUrl;

    // For requests, to link to a channel
    private Long channelId;

    // For responses, to show channel name (optional, can be populated by service)
    private String channelName;

    private OffsetDateTime createdAt; // Include for responses

    private OffsetDateTime updatedAt; // Include for responses
}
