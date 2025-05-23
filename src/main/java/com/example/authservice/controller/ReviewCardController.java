package com.example.authservice.controller;

import com.example.authservice.dto.ReviewCardDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.service.ReviewCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review-cards")
@RequiredArgsConstructor
public class ReviewCardController {

    private final ReviewCardService reviewCardService;

    // Helper to extract UserDetails from Authentication
    private UserDetails getCurrentUserDetails(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null; // Or throw exception if UserDetails must always be present
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        // Handle cases where principal might be a String (e.g. in tests or specific configs)
        // For this application, UserDetailsServiceImpl ensures UserDetails is the principal.
        return null;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewCardDto> createReviewCard(@Valid @RequestBody ReviewCardDto reviewCardDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewCardDto createdReviewCard = reviewCardService.createReviewCard(reviewCardDto, currentUser);
        return new ResponseEntity<>(createdReviewCard, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewCardDto> getReviewCardById(@PathVariable Long id) {
        ReviewCardDto reviewCard = reviewCardService.getReviewCardById(id);
        return ResponseEntity.ok(reviewCard);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewCardDto>> getAllReviewCards(@PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewCardDto> reviewCards = reviewCardService.getAllReviewCards(pageable);
        return ResponseEntity.ok(reviewCards);
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewCardDto>> getReviewCardsByMerchantId(@PathVariable Long merchantId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewCardDto> reviewCards = reviewCardService.getReviewCardsByMerchantId(merchantId, pageable);
        return ResponseEntity.ok(reviewCards);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewCardDto> updateReviewCard(@PathVariable Long id, @Valid @RequestBody ReviewCardDto reviewCardDto, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewCardDto updatedReviewCard = reviewCardService.updateReviewCard(id, reviewCardDto, currentUser);
        return ResponseEntity.ok(updatedReviewCard);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteReviewCard(@PathVariable Long id, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        reviewCardService.deleteReviewCard(id, currentUser);
        return ResponseEntity.ok(new SuccessResponseDto<>("Review card deleted successfully with id: " + id));
    }

    @PostMapping("/{reviewCardId}/collections/{collectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewCardDto> addCollectionToReviewCard(@PathVariable Long reviewCardId, @PathVariable Long collectionId, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewCardDto updatedReviewCard = reviewCardService.addCollectionToReviewCard(reviewCardId, collectionId, currentUser);
        return ResponseEntity.ok(updatedReviewCard);
    }

    @DeleteMapping("/{reviewCardId}/collections/{collectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewCardDto> removeCollectionFromReviewCard(@PathVariable Long reviewCardId, @PathVariable Long collectionId, Authentication authentication) {
        UserDetails currentUser = getCurrentUserDetails(authentication);
        ReviewCardDto updatedReviewCard = reviewCardService.removeCollectionFromReviewCard(reviewCardId, collectionId, currentUser);
        return ResponseEntity.ok(updatedReviewCard);
    }
}
