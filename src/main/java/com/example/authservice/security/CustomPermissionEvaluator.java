package com.example.authservice.security;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("customPermissionEvaluator")
public class CustomPermissionEvaluator {

    @Autowired
    private UserRepository userRepository; // Use repository to fetch user if needed

    public boolean isSelfOrAdmin(Authentication authentication, Long targetUserId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Check for ADMIN role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        // Check if the authenticated user's ID matches the targetUserId
        // This requires UserDetails to hold the ID or fetching the user by username
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        return currentUser != null && currentUser.getId().equals(targetUserId);
    }
}
