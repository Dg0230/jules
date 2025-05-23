package com.example.authservice.controller;

import com.example.authservice.dto.MaterialCollectionDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.MaterialCollectionService;
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
@RequestMapping("/api/material-collections")
@RequiredArgsConstructor
public class MaterialCollectionController {

    private final MaterialCollectionService materialCollectionService;
    private final UserRepository userRepository; // To get current user for creatorId

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
    @PreAuthorize("hasRole('ADMIN')") // Simplified: Admin creates collections
    // If any authenticated user can create: @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialCollectionDto> createMaterialCollection(@Valid @RequestBody MaterialCollectionDto collectionDto, Authentication authentication) {
        Long creatorId = getCurrentUserId(authentication);
        MaterialCollectionDto createdCollection = materialCollectionService.createMaterialCollection(collectionDto, creatorId);
        return new ResponseEntity<>(createdCollection, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialCollectionDto> getMaterialCollectionById(@PathVariable Long id) {
        MaterialCollectionDto collection = materialCollectionService.getMaterialCollectionById(id);
        return ResponseEntity.ok(collection);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MaterialCollectionDto>> getAllMaterialCollections(@PageableDefault(size = 20) Pageable pageable) {
        Page<MaterialCollectionDto> collections = materialCollectionService.getAllMaterialCollections(pageable);
        return ResponseEntity.ok(collections);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialCollectionDto> updateMaterialCollection(@PathVariable Long id, @Valid @RequestBody MaterialCollectionDto collectionDto) {
        // Creator ID is not updated here, ownership usually doesn't change via simple update
        MaterialCollectionDto updatedCollection = materialCollectionService.updateMaterialCollection(id, collectionDto);
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteMaterialCollection(@PathVariable Long id) {
        materialCollectionService.deleteMaterialCollection(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Material collection deleted successfully with id: " + id));
    }

    @PostMapping("/{collectionId}/materials/{materialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialCollectionDto> addMaterialToCollection(@PathVariable Long collectionId, @PathVariable Long materialId) {
        MaterialCollectionDto updatedCollection = materialCollectionService.addMaterialToCollection(collectionId, materialId);
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{collectionId}/materials/{materialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialCollectionDto> removeMaterialFromCollection(@PathVariable Long collectionId, @PathVariable Long materialId) {
        MaterialCollectionDto updatedCollection = materialCollectionService.removeMaterialFromCollection(collectionId, materialId);
        return ResponseEntity.ok(updatedCollection);
    }
}
