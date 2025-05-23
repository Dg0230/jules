package com.example.authservice.service.impl;

import com.example.authservice.dto.CreateReviewRequestDto;
import com.example.authservice.dto.ReviewDto;
import com.example.authservice.dto.UpdateReviewStatusDto;
import com.example.authservice.entity.Merchant;
import com.example.authservice.entity.Review;
import com.example.authservice.entity.ReviewCard;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.MerchantRepository;
import com.example.authservice.repository.ReviewCardRepository;
import com.example.authservice.repository.ReviewRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewCardRepository reviewCardRepository;
    private final MerchantRepository merchantRepository; // Added for denormalized merchant_id

    @Override
    @Transactional
    public ReviewDto createReview(CreateReviewRequestDto createReviewDto, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUser.getUsername()));
        ReviewCard reviewCard = reviewCardRepository.findById(createReviewDto.getReviewCardId())
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCard", "id", createReviewDto.getReviewCardId()));
        
        // Denormalize merchant from ReviewCard, or ensure it's explicitly set if schema allows null on ReviewCard's merchant_id
        Merchant merchant = reviewCard.getMerchant();
        if (merchant == null) {
            throw new BadRequestException("ReviewCard with ID " + reviewCard.getId() + " is not associated with a valid merchant.");
        }


        Review review = new Review();
        review.setUser(user);
        review.setReviewCard(reviewCard);
        review.setMerchant(merchant); // Set denormalized merchant
        review.setRating(createReviewDto.getRating());
        review.setTextContent(createReviewDto.getTextContent());
        if (createReviewDto.getImageUrls() != null) {
            review.setImageUrls(createReviewDto.getImageUrls());
        }
        if (createReviewDto.getVideoUrls() != null) {
            review.setVideoUrls(createReviewDto.getVideoUrls());
        }
        review.setPlatform(createReviewDto.getPlatform());
        review.setPlatformReviewId(createReviewDto.getPlatformReviewId());
        review.setSharedAt(createReviewDto.getSharedAt());
        review.setStatus("pending_approval"); // Default status

        Review savedReview = reviewRepository.save(review);
        return mapEntityToDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return mapEntityToDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return reviewRepository.findByUserId(userId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByMerchantId(Long merchantId, Pageable pageable) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant", "id", merchantId);
        }
        return reviewRepository.findByMerchantId(merchantId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByReviewCardId(Long reviewCardId, Pageable pageable) {
        if (!reviewCardRepository.existsById(reviewCardId)) {
            throw new ResourceNotFoundException("ReviewCard", "id", reviewCardId);
        }
        return reviewRepository.findByReviewCardId(reviewCardId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto, UserDetails currentUser) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        
        // Authorization check (isOwner or Admin) is expected to be handled by @PreAuthorize in controller

        // Fields that a user might update (if allowed)
        if (reviewDto.getRating() != null) review.setRating(reviewDto.getRating());
        if (reviewDto.getTextContent() != null) review.setTextContent(reviewDto.getTextContent());
        if (reviewDto.getImageUrls() != null) review.setImageUrls(reviewDto.getImageUrls());
        if (reviewDto.getVideoUrls() != null) review.setVideoUrls(reviewDto.getVideoUrls());
        // Other fields like platform, platformReviewId, sharedAt might also be updatable by user or admin
        if (reviewDto.getPlatform() != null) review.setPlatform(reviewDto.getPlatform());
        if (reviewDto.getPlatformReviewId() != null) review.setPlatformReviewId(reviewDto.getPlatformReviewId());
        if (reviewDto.getSharedAt() != null) review.setSharedAt(reviewDto.getSharedAt());

        // Status update should typically go through its own method for stricter control
        // if (reviewDto.getStatus() != null && currentUser has ADMIN_ROLE) review.setStatus(reviewDto.getStatus());

        Review updatedReview = reviewRepository.save(review);
        return mapEntityToDto(updatedReview);
    }

    @Override
    @Transactional
    public ReviewDto updateReviewStatus(Long id, UpdateReviewStatusDto statusDto, UserDetails currentUser) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        
        // Authorization check (e.g. Admin or specific Merchant role) handled by @PreAuthorize

        review.setStatus(statusDto.getStatus());
        Review updatedReview = reviewRepository.save(review);
        return mapEntityToDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, UserDetails currentUser) {
        // Authorization check (isOwner or Admin) handled by @PreAuthorize
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        reviewRepository.deleteById(id);
    }

    // --- Helper Mapper ---
    private ReviewDto mapEntityToDto(Review review) {
        if (review == null) return null;
        ReviewDto dto = new ReviewDto();
        BeanUtils.copyProperties(review, dto, "user", "reviewCard", "merchant");

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUsername(review.getUser().getUsername());
        }
        if (review.getReviewCard() != null) {
            dto.setReviewCardId(review.getReviewCard().getId());
            dto.setReviewCardName(review.getReviewCard().getName()); // Or cardIdentifier
        }
        if (review.getMerchant() != null) {
            dto.setMerchantId(review.getMerchant().getId());
            dto.setMerchantName(review.getMerchant().getName());
        }
        return dto;
    }
}
