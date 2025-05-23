package com.example.authservice.security;

import com.example.authservice.entity.Review;
import com.example.authservice.entity.User;
import com.example.authservice.repository.ReviewRepository;
import com.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("reviewSecurityService")
public class ReviewSecurityService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean isOwner(Authentication authentication, Long reviewId) {
        if (authentication == null || !authentication.isAuthenticated() || reviewId == null) {
            return false;
        }

        String currentUsername = authentication.getName();
        Optional<User> optionalCurrentUser = userRepository.findByUsername(currentUsername);
        if (optionalCurrentUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + currentUsername);
        }
        Long currentUserId = optionalCurrentUser.get().getId();

        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isEmpty()) {
            return false; // Or throw ResourceNotFoundException, but for PreAuthorize, returning false is typical
        }
        Review review = optionalReview.get();

        return review.getUser() != null && review.getUser().getId().equals(currentUserId);
    }
}
