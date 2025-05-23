package com.example.authservice.controller;

import com.example.authservice.dto.MaterialDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository; // To get current user
import com.example.authservice.service.MaterialService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final UserRepository userRepository; // Inject UserRepository

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated or is anonymous");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username + " from token."));
        return currentUser.getId();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can upload
    public ResponseEntity<MaterialDto> createMaterial(@Valid @RequestBody MaterialDto materialDto, Authentication authentication) {
        Long uploaderId = getCurrentUserId(authentication);
        MaterialDto createdMaterial = materialService.createMaterial(materialDto, uploaderId);
        return new ResponseEntity<>(createdMaterial, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialDto> getMaterialById(@PathVariable Long id) {
        MaterialDto material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MaterialDto>> getAllMaterials(@PageableDefault(size = 20) Pageable pageable) {
        Page<MaterialDto> materials = materialService.getAllMaterials(pageable);
        return ResponseEntity.ok(materials);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Simplified: Only ADMIN can update any material
    // For ownership: @PreAuthorize("hasRole('ADMIN') or @materialSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<MaterialDto> updateMaterial(@PathVariable Long id, @Valid @RequestBody MaterialDto materialDto, Authentication authentication) {
        // If ownership check was desired, uploaderId would be used for that check in a security service.
        // For admin-only update, uploaderId from principal is not strictly needed for the update itself unless business logic demands it.
        // The service method `updateMaterial` takes `uploaderId` - this could be the original uploader or current user if re-assigning.
        // For this simplified scenario, let's assume an admin might be "acting on behalf of" or just modifying.
        // We can pass the current admin's ID as the uploaderId, or allow DTO to specify new uploaderId if that's a feature.
        // Sticking to the service signature:
        Long currentUserId = getCurrentUserId(authentication); // This would be admin's ID
        MaterialDto updatedMaterial = materialService.updateMaterial(id, materialDto, currentUserId);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Simplified: Only ADMIN can delete any material
    // For ownership: @PreAuthorize("hasRole('ADMIN') or @materialSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<SuccessResponseDto<Object>> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Material deleted successfully with id: " + id));
    }
}
