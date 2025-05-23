package com.example.authservice.service.impl;

import com.example.authservice.dto.MaterialCollectionDto;
import com.example.authservice.dto.MaterialDto;
import com.example.authservice.dto.ReviewCardDto;
import com.example.authservice.entity.Material;
import com.example.authservice.entity.MaterialCollection;
import com.example.authservice.entity.Merchant;
import com.example.authservice.entity.ReviewCard;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.MaterialCollectionRepository;
import com.example.authservice.repository.MerchantRepository;
import com.example.authservice.repository.ReviewCardRepository;
import com.example.authservice.service.ReviewCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewCardServiceImpl implements ReviewCardService {

    private final ReviewCardRepository reviewCardRepository;
    private final MerchantRepository merchantRepository;
    private final MaterialCollectionRepository materialCollectionRepository;

    @Override
    @Transactional
    public ReviewCardDto createReviewCard(ReviewCardDto reviewCardDto, UserDetails currentUser) {
        // Authorization check (e.g., if currentUser is admin or owns the merchant) would happen in controller or a security service
        Merchant merchant = merchantRepository.findById(reviewCardDto.getMerchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", reviewCardDto.getMerchantId()));

        if (reviewCardDto.getCardIdentifier() != null && reviewCardRepository.existsByCardIdentifier(reviewCardDto.getCardIdentifier())) {
            throw new BadRequestException("ReviewCard cardIdentifier '" + reviewCardDto.getCardIdentifier() + "' is already taken.");
        }
         if (reviewCardRepository.existsByName(reviewCardDto.getName())) {
            throw new BadRequestException("ReviewCard name '" + reviewCardDto.getName() + "' is already taken for this merchant or globally (adjust as per business rule).");
        }


        ReviewCard reviewCard = new ReviewCard();
        mapDtoToEntity(reviewCardDto, reviewCard); // Map basic fields
        reviewCard.setMerchant(merchant);

        if (!CollectionUtils.isEmpty(reviewCardDto.getCollectionIds())) {
            Set<MaterialCollection> collections = new HashSet<>(materialCollectionRepository.findAllById(reviewCardDto.getCollectionIds()));
            if (collections.size() != reviewCardDto.getCollectionIds().size()) {
                throw new BadRequestException("One or more material collection IDs provided were not found.");
            }
            reviewCard.setMaterialCollections(collections);
        } else {
            reviewCard.setMaterialCollections(new HashSet<>());
        }
        
        if (reviewCard.getStatus() == null) reviewCard.setStatus("inactive");


        ReviewCard savedReviewCard = reviewCardRepository.save(reviewCard);
        return mapEntityToDto(savedReviewCard);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewCardDto getReviewCardById(Long id) {
        ReviewCard reviewCard = reviewCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCard", "id", id));
        return mapEntityToDto(reviewCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCardDto> getAllReviewCards(Pageable pageable) {
        return reviewCardRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewCardDto> getReviewCardsByMerchantId(Long merchantId, Pageable pageable) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant", "id", merchantId);
        }
        return reviewCardRepository.findByMerchantId(merchantId, pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public ReviewCardDto updateReviewCard(Long id, ReviewCardDto reviewCardDto, UserDetails currentUser) {
        ReviewCard reviewCard = reviewCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCard", "id", id));

        // Authorization check (e.g., if currentUser is admin or owns this review card's merchant)

        if (reviewCardDto.getCardIdentifier() != null && !reviewCard.getCardIdentifier().equals(reviewCardDto.getCardIdentifier()) &&
            reviewCardRepository.existsByCardIdentifier(reviewCardDto.getCardIdentifier())) {
            throw new BadRequestException("ReviewCard cardIdentifier '" + reviewCardDto.getCardIdentifier() + "' is already taken.");
        }
        if (reviewCardDto.getName() != null && !reviewCard.getName().equals(reviewCardDto.getName()) &&
             reviewCardRepository.existsByName(reviewCardDto.getName())) { // Simplified check, may need scoping to merchant
            throw new BadRequestException("ReviewCard name '" + reviewCardDto.getName() + "' is already taken.");
        }


        mapDtoToEntityForUpdate(reviewCardDto, reviewCard);

        if (reviewCardDto.getMerchantId() != null && !reviewCard.getMerchant().getId().equals(reviewCardDto.getMerchantId())) {
            Merchant newMerchant = merchantRepository.findById(reviewCardDto.getMerchantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", reviewCardDto.getMerchantId()));
            reviewCard.setMerchant(newMerchant);
        }

        if (reviewCardDto.getCollectionIds() != null) {
            Set<MaterialCollection> collections = new HashSet<>();
            if (!reviewCardDto.getCollectionIds().isEmpty()) {
                collections.addAll(materialCollectionRepository.findAllById(reviewCardDto.getCollectionIds()));
                if (collections.size() != reviewCardDto.getCollectionIds().size()) {
                     throw new BadRequestException("One or more material collection IDs provided for update were not found.");
                }
            }
            reviewCard.setMaterialCollections(collections);
        }

        ReviewCard updatedReviewCard = reviewCardRepository.save(reviewCard);
        return mapEntityToDto(updatedReviewCard);
    }

    @Override
    @Transactional
    public void deleteReviewCard(Long id, UserDetails currentUser) {
        // Authorization check
        if (!reviewCardRepository.existsById(id)) {
            throw new ResourceNotFoundException("ReviewCard", "id", id);
        }
        reviewCardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReviewCardDto addCollectionToReviewCard(Long reviewCardId, Long collectionId, UserDetails currentUser) {
        ReviewCard reviewCard = reviewCardRepository.findById(reviewCardId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCard", "id", reviewCardId));
        MaterialCollection collection = materialCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", collectionId));

        // Authorization check
        if (reviewCard.getMaterialCollections().contains(collection)) {
            throw new BadRequestException("MaterialCollection already associated with this ReviewCard.");
        }
        reviewCard.addMaterialCollection(collection);
        reviewCardRepository.save(reviewCard);
        return mapEntityToDto(reviewCard);
    }

    @Override
    @Transactional
    public ReviewCardDto removeCollectionFromReviewCard(Long reviewCardId, Long collectionId, UserDetails currentUser) {
        ReviewCard reviewCard = reviewCardRepository.findById(reviewCardId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCard", "id", reviewCardId));
        MaterialCollection collection = materialCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", collectionId));

        // Authorization check
        if (!reviewCard.getMaterialCollections().contains(collection)) {
            throw new BadRequestException("MaterialCollection is not associated with this ReviewCard.");
        }
        reviewCard.removeMaterialCollection(collection);
        reviewCardRepository.save(reviewCard);
        return mapEntityToDto(reviewCard);
    }

    // --- Helper Mappers ---
    private ReviewCardDto mapEntityToDto(ReviewCard reviewCard) {
        if (reviewCard == null) return null;
        ReviewCardDto dto = new ReviewCardDto();
        BeanUtils.copyProperties(reviewCard, dto, "merchant", "materialCollections");

        if (reviewCard.getMerchant() != null) {
            dto.setMerchantId(reviewCard.getMerchant().getId());
            dto.setMerchantName(reviewCard.getMerchant().getName());
        }
        if (reviewCard.getMaterialCollections() != null) {
            dto.setMaterialCollections(reviewCard.getMaterialCollections().stream()
                    .map(this::mapMaterialCollectionEntityToDto) // Nested DTO mapping
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private void mapDtoToEntity(ReviewCardDto dto, ReviewCard entity) {
        // Exclude id, merchant, materialCollections, createdAt, updatedAt (handled separately or by JPA)
        BeanUtils.copyProperties(dto, entity, "id", "merchantId", "merchantName", "materialCollections", "collectionIds", "createdAt", "updatedAt");
    }
    
    private void mapDtoToEntityForUpdate(ReviewCardDto dto, ReviewCard entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getCardIdentifier() != null) entity.setCardIdentifier(dto.getCardIdentifier());
        if (dto.getQrCodeUrl() != null) entity.setQrCodeUrl(dto.getQrCodeUrl());
        if (dto.getNfcTagId() != null) entity.setNfcTagId(dto.getNfcTagId());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getAiPromptCustom() != null) entity.setAiPromptCustom(dto.getAiPromptCustom());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        // merchantId and collectionIds are handled separately in the update method
    }


    // Helper for nested MaterialCollectionDto
    private MaterialCollectionDto mapMaterialCollectionEntityToDto(MaterialCollection collection) {
        if (collection == null) return null;
        MaterialCollectionDto dto = new MaterialCollectionDto();
        BeanUtils.copyProperties(collection, dto, "creator", "materials"); // Avoid deep copy of materials here if not needed or map them carefully

        if (collection.getCreator() != null) {
            dto.setCreatorId(collection.getCreator().getId());
            dto.setCreatorUsername(collection.getCreator().getUsername());
        }
        // For simplicity, this nested DTO won't itself contain another layer of MaterialDto for now,
        // unless MaterialCollectionDto is enhanced or a specific "DetailedMaterialCollectionDto" is used.
        // The current MaterialCollectionDto structure includes Set<MaterialDto> materials.
        if (collection.getMaterials() != null) {
            dto.setMaterials(collection.getMaterials().stream()
                .map(this::mapMaterialEntityToDto) // Re-use Material mapper
                .collect(Collectors.toSet()));
        }
        return dto;
    }
    
    // Helper for nested MaterialDto inside MaterialCollectionDto
    private MaterialDto mapMaterialEntityToDto(Material material) {
        if (material == null) return null;
        MaterialDto dto = new MaterialDto();
        BeanUtils.copyProperties(material, dto, "uploader", "collections");
        if (material.getUploader() != null) {
            dto.setUploaderId(material.getUploader().getId());
            dto.setUploaderUsername(material.getUploader().getUsername());
        }
        return dto;
    }
}
