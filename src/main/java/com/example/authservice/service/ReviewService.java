package com.example.authservice.service;

import com.example.authservice.dto.CreateReviewRequestDto;
import com.example.authservice.dto.ReviewDto;
import com.example.authservice.dto.UpdateReviewStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface ReviewService {
    ReviewDto createReview(CreateReviewRequestDto createReviewDto, UserDetails currentUser);
    ReviewDto getReviewById(Long id);
    Page<ReviewDto> getAllReviews(Pageable pageable); // Admin view
    Page<ReviewDto> getReviewsByUserId(Long userId, Pageable pageable); // For /my-reviews, or admin viewing specific user's reviews
    Page<ReviewDto> getReviewsByMerchantId(Long merchantId, Pageable pageable);
    Page<ReviewDto> getReviewsByReviewCardId(Long reviewCardId, Pageable pageable);
    ReviewDto updateReview(Long id, ReviewDto reviewDto, UserDetails currentUser); // User updating their own review
    ReviewDto updateReviewStatus(Long id, UpdateReviewStatusDto statusDto, UserDetails currentUser); // Admin or merchant updating status
    void deleteReview(Long id, UserDetails currentUser);
}
