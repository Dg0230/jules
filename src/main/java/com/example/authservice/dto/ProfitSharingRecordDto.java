package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSharingRecordDto {

    private Long id;

    // Order details
    private Long orderId;
    private String orderNumber;

    // Channel details
    private Long channelId;
    private String channelName;

    // Merchant details (optional)
    private Long merchantId;
    private String merchantName;

    private BigDecimal amount;
    private String currency;
    private BigDecimal commissionRateSnapshot;
    private String status;
    private String calculationDetails;
    private OffsetDateTime paidAt;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
