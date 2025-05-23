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
public class CreateOrderRequestDto {

    private Long userId; // Optional: If not provided, service might use current authenticated user.
                        // Or could be an admin creating an order for a specific user.

    private Long merchantId; // Optional

    private Long channelId; // Optional

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency = "CNY";

    @NotBlank(message = "Package name is required")
    @Size(max = 255, message = "Package name must be less than 255 characters")
    private String packageName;

    private String packageDetails; // Can be JSON or long text

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;
}
