package com.example.authservice.service.impl;

import com.example.authservice.dto.MaterialDto;
import com.example.authservice.entity.Material;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.MaterialRepository;
import com.example.authservice.repository.UserRepository; // To fetch User entity for uploader
import com.example.authservice.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaterialDto createMaterial(MaterialDto materialDto, Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploaderId));

        if (materialDto.getName() != null && materialRepository.existsByName(materialDto.getName())) {
            throw new BadRequestException("Material name '" + materialDto.getName() + "' is already taken.");
        }

        Material material = new Material();
        mapDtoToEntity(materialDto, material); // Map DTO fields
        material.setUploader(uploader); // Set uploader

        // Set default status if not provided
        if (material.getStatus() == null) {
            material.setStatus("pending");
        }
        if (material.getUsageCount() == null) {
            material.setUsageCount(0L);
        }

        Material savedMaterial = materialRepository.save(material);
        return mapEntityToDto(savedMaterial);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialDto getMaterialById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));
        return mapEntityToDto(material);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialDto> getAllMaterials(Pageable pageable) {
        return materialRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public MaterialDto updateMaterial(Long id, MaterialDto materialDto, Long uploaderId) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));

        // Basic ownership check or admin override would typically happen here or at controller level.
        // For simplified scenario, we assume authorization is handled by controller.
        // If uploader is being changed, validate the new uploaderId
        if (materialDto.getUploaderId() != null && !materialDto.getUploaderId().equals(material.getUploader().getId())) {
             User newUploader = userRepository.findById(materialDto.getUploaderId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", materialDto.getUploaderId()));
             material.setUploader(newUploader);
        }


        // Check if new name is taken by another material
        if (materialDto.getName() != null && !material.getName().equals(materialDto.getName()) && materialRepository.existsByName(materialDto.getName())) {
            throw new BadRequestException("Material name '" + materialDto.getName() + "' is already taken by another material.");
        }

        // Update properties from DTO
        mapDtoToEntityForUpdate(materialDto, material);


        Material updatedMaterial = materialRepository.save(material);
        return mapEntityToDto(updatedMaterial);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Material", "id", id);
        }
        // Consider implications: if material is part of collections, how to handle?
        // Current setup: MaterialCollection is owner, so removing Material might require
        // removing it from all collections first, or handling DB constraint violations.
        // For now, direct delete. If `collection_materials` has FK constraints, this might fail if material is in use.
        // A cleaner way is to iterate through material.getCollections() and remove material from each collection.
        Material material = materialRepository.findById(id).get(); // Should exist due to check above
        material.getCollections().forEach(collection -> collection.getMaterials().remove(material)); // Break relationship
        materialRepository.deleteById(id);
    }

    // --- Helper Mappers ---
    private MaterialDto mapEntityToDto(Material material) {
        if (material == null) return null;
        MaterialDto dto = new MaterialDto();
        BeanUtils.copyProperties(material, dto, "uploader", "collections");
        if (material.getUploader() != null) {
            dto.setUploaderId(material.getUploader().getId());
            dto.setUploaderUsername(material.getUploader().getUsername());
        }
        return dto;
    }

    private void mapDtoToEntity(MaterialDto materialDto, Material material) {
        // Exclude id, uploader (handled separately), collections, createdAt, updatedAt
        BeanUtils.copyProperties(materialDto, material, "id", "uploaderId", "uploaderUsername", "createdAt", "updatedAt", "collections");
    }
    
    private void mapDtoToEntityForUpdate(MaterialDto dto, Material entity) {
        // Selectively copy fields that are allowed to be updated
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getType() != null) entity.setType(dto.getType());
        if (dto.getUrl() != null) entity.setUrl(dto.getUrl());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getUsageCount() != null) entity.setUsageCount(dto.getUsageCount());
        if (dto.getLastUsedAt() != null) entity.setLastUsedAt(dto.getLastUsedAt());
        if (dto.getExpiresAt() != null) entity.setExpiresAt(dto.getExpiresAt());
        // uploaderId should be handled carefully, possibly not in a general update or only by admin
    }
}
