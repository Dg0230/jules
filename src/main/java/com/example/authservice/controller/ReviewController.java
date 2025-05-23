package com.example.authservice.controller;

import com.example.authservice.dto.CreateReviewRequestDto;
import com.example.authservice.dto.ReviewDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.dto.UpdateReviewStatusDto;
import com.example.authservice.entity.User; // For getCurrentUserId helper
import com.example.authservice.repository.UserRepository; // For getCurrentUserId helper
import com.example.authservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository; // For getCurrentUserId

    // Helper to extract UserDetails from Authentication
    private UserDetails getCurrentUserDetails(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated or is anonymous");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        throw new AuthenticationCredentialsNotFoundException("Principal is not an instance of UserDetails");
    }
    
    // Helper to get current authenticated user's ID
    private Long getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = getCurrentUserDetails(authentication);
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        return currentUser.getId();
    }


    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewRequestDto createReviewDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewDto createdReview = reviewService.createReview(createReviewDto, currentUser);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(@PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewDto>> getMyReviews(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        Page<ReviewDto> reviews = reviewService.getReviewsByUserId(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')") // Simplified from hasAnyRole('ADMIN', 'MERCHANT_OWNER')
    public ResponseEntity<Page<ReviewDto>> getReviewsByMerchantId(@PathVariable Long merchantId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getReviewsByMerchantId(merchantId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/by-card/{reviewCardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewDto>> getReviewsByReviewCardId(@PathVariable Long reviewCardId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getReviewsByReviewCardId(reviewCardId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@reviewSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDto reviewDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewDto updatedReview = reviewService.updateReview(id, reviewDto, currentUser);
        return ResponseEntity.ok(updatedReview);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDto> updateReviewStatus(@PathVariable Long id, @Valid @RequestBody UpdateReviewStatusDto statusDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication); // Admin user performing the action
        ReviewDto updatedReview = reviewService.updateReviewStatus(id, statusDto, currentUser);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<SuccessResponseDto<Object>> deleteReview(@PathVariable Long id, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        reviewService.deleteReview(id, currentUser);
        return ResponseEntity.ok(new SuccessResponseDto<>("Review deleted successfully with id: " + id));
    }
}
