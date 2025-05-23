package com.example.authservice.repository;

import com.example.authservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByUserId(Long userId, Pageable pageable);
    Page<Review> findByReviewCardId(Long reviewCardId, Pageable pageable);
    Page<Review> findByMerchantId(Long merchantId, Pageable pageable);
    Page<Review> findByStatus(String status, Pageable pageable);
}
