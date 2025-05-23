package com.example.authservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDto {

    private Long id; // Include for responses

    @NotBlank(message = "Channel name is required")
    @Size(max = 255, message = "Channel name must be less than 255 characters")
    private String name;

    @Size(max = 255, message = "Contact person name must be less than 255 characters")
    private String contactPerson;

    @Size(max = 50, message = "Contact phone must be less than 50 characters")
    private String contactPhone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., active, inactive

    @NotNull(message = "Account balance cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Account balance must be non-negative")
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @NotNull(message = "Total profit share available cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total profit share available must be non-negative")
    private BigDecimal totalProfitShareAvailable = BigDecimal.ZERO;

    @NotNull(message = "Total profit shared cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total profit shared must be non-negative")
    private BigDecimal totalProfitShared = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be non-negative")
    private BigDecimal commissionRate; // e.g., 0.10 for 10%

    private OffsetDateTime createdAt; // Include for responses

    private OffsetDateTime updatedAt; // Include for responses
}
