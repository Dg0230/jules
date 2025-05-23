package com.example.authservice.repository;

import com.example.authservice.entity.ProfitSharingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfitSharingRecordRepository extends JpaRepository<ProfitSharingRecord, Long> {
    Page<ProfitSharingRecord> findByOrderId(Long orderId, Pageable pageable);
    Page<ProfitSharingRecord> findByChannelId(Long channelId, Pageable pageable);
    Page<ProfitSharingRecord> findByMerchantId(Long merchantId, Pageable pageable);
    Page<ProfitSharingRecord> findByStatus(String status, Pageable pageable);
}
