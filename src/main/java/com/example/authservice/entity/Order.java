package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orders") // Matches the table name in schema.sql
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 64)
    private String orderNumber; // Unique order identifier

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // User who placed/owns the order
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id") // Nullable, if order is for a specific merchant
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id") // Nullable, if order is for a specific channel
    private Channel channel;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 10, nullable = false)
    private String currency = "CNY"; // Default currency

    @Column(length = 50, nullable = false)
    private String status = "PENDING"; // e.g., PENDING, COMPLETED, FAILED, REFUNDED

    @Column(name = "payment_gateway", length = 100)
    private String paymentGateway; // e.g., Stripe, WeChat Pay, Alipay

    @Column(name = "payment_gateway_transaction_id", length = 255)
    private String paymentGatewayTransactionId;

    @Column(name = "package_name", length = 255) // e.g., "100 reviews pack", "Monthly Subscription Tier 1"
    private String packageName;

    @Lob // For potentially large JSON or text details
    @Column(name = "package_details")
    private String packageDetails;
    
    @Lob // Added 'notes' from schema.sql as it's useful for general order notes
    private String notes;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Constraint: either merchant_id or channel_id is set, but not both.
    // This is better enforced at service layer or database level if complex.
    // For JPA, can use @PrePersist and @PreUpdate lifecycle callbacks to validate.
    // Or a custom validator. For now, this will be handled in the service layer.
}
