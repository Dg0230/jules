package com.example.authservice.service;

import com.example.authservice.dto.ReviewCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface ReviewCardService {
    ReviewCardDto createReviewCard(ReviewCardDto reviewCardDto, UserDetails currentUser);
    ReviewCardDto getReviewCardById(Long id);
    Page<ReviewCardDto> getAllReviewCards(Pageable pageable);
    Page<ReviewCardDto> getReviewCardsByMerchantId(Long merchantId, Pageable pageable);
    ReviewCardDto updateReviewCard(Long id, ReviewCardDto reviewCardDto, UserDetails currentUser);
    void deleteReviewCard(Long id, UserDetails currentUser);

    ReviewCardDto addCollectionToReviewCard(Long reviewCardId, Long collectionId, UserDetails currentUser);
    ReviewCardDto removeCollectionFromReviewCard(Long reviewCardId, Long collectionId, UserDetails currentUser);
}
