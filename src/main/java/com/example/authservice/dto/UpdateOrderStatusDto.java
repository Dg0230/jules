package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDto {

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status; // e.g., COMPLETED, FAILED, REFUNDED

    @Size(max = 255, message = "Payment Gateway Transaction ID must be less than 255 characters")
    private String paymentGatewayTransactionId; // Optional, can be updated upon payment completion
}
