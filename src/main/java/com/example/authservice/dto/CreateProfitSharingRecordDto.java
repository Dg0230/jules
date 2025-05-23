package com.example.authservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfitSharingRecordDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Channel ID is required")
    private Long channelId;

    private Long merchantId; // Optional

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency = "CNY";

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // Optional, defaults to PENDING in service

    private String calculationDetails; // JSON or text

    private BigDecimal commissionRateSnapshot; // Optional, for record keeping

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;
}
