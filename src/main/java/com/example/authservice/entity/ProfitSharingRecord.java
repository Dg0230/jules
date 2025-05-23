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
@Table(name = "profit_sharing_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSharingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel; // Channel receiving the profit share

    @ManyToOne(fetch = FetchType.LAZY) // Nullable
    @JoinColumn(name = "merchant_id")
    private Merchant merchant; // Optional: if profit share is related to a specific merchant

    @Column(name = "amount", nullable = false, precision = 19, scale = 4) // Renamed from shared_amount
    private BigDecimal amount;

    @Column(length = 10, nullable = false)
    private String currency = "CNY";

    @Column(name = "commission_rate_snapshot", precision = 5, scale = 2) // From schema
    private BigDecimal commissionRateSnapshot;

    @Column(length = 50, nullable = false)
    private String status = "PENDING"; // e.g., PENDING, PAID, FAILED

    @Lob
    @Column(name = "calculation_details") // JSON or text
    private String calculationDetails;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime paidAt;
    
    @Lob // From schema
    private String notes;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
}
