package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private String orderNumber;

    // User details
    private Long userId;
    private String username; // User's username

    // Merchant details (optional)
    private Long merchantId;
    private String merchantName;

    // Channel details (optional)
    private Long channelId;
    private String channelName;

    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentGateway;
    private String paymentGatewayTransactionId;
    private String packageName;
    private String packageDetails;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
