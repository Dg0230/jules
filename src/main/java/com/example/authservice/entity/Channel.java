package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column // Added based on task description
    private String address;

    @Column(length = 50, nullable = false)
    private String status = "active"; // Default status

    @Column(name = "account_balance", precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000") // Added
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @Column(name = "total_profit_share_available", precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000") // Added
    private BigDecimal totalProfitShareAvailable = BigDecimal.ZERO;

    @Column(name = "total_profit_shared", precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000") // Added
    private BigDecimal totalProfitShared = BigDecimal.ZERO;
    
    @Column(name = "commission_rate", precision = 5, scale = 2) // Kept from original schema.sql
    private BigDecimal commissionRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Merchant> merchants = new ArrayList<>();

    // Convenience methods for merchants if needed
    public void addMerchant(Merchant merchant) {
        merchants.add(merchant);
        merchant.setChannel(this);
    }

    public void removeMerchant(Merchant merchant) {
        merchants.remove(merchant);
        merchant.setChannel(null);
    }
}
