package com.example.authservice.repository;

import com.example.authservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByMerchantId(Long merchantId, Pageable pageable);
    Page<Order> findByChannelId(Long channelId, Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);
    boolean existsByOrderNumber(String orderNumber);
}
