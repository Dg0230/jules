package com.example.authservice.service.impl;

import com.example.authservice.dto.MaterialCollectionDto;
import com.example.authservice.dto.MaterialDto;
import com.example.authservice.entity.Material;
import com.example.authservice.entity.MaterialCollection;
import com.example.authservice.entity.User;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.MaterialCollectionRepository;
import com.example.authservice.repository.MaterialRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.MaterialCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialCollectionServiceImpl implements MaterialCollectionService {

    private final MaterialCollectionRepository materialCollectionRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaterialCollectionDto createMaterialCollection(MaterialCollectionDto collectionDto, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User (Creator)", "id", creatorId));

        if (collectionDto.getName() != null && materialCollectionRepository.existsByName(collectionDto.getName())) {
            throw new BadRequestException("MaterialCollection name '" + collectionDto.getName() + "' is already taken.");
        }

        MaterialCollection collection = new MaterialCollection();
        mapDtoToEntity(collectionDto, collection); // Map basic fields
        collection.setCreator(creator);

        if (!CollectionUtils.isEmpty(collectionDto.getMaterialIds())) {
            Set<Material> materials = new HashSet<>(materialRepository.findAllById(collectionDto.getMaterialIds()));
            if (materials.size() != collectionDto.getMaterialIds().size()) {
                throw new BadRequestException("One or more material IDs provided were not found.");
            }
            collection.setMaterials(materials);
        } else {
            collection.setMaterials(new HashSet<>());
        }


        MaterialCollection savedCollection = materialCollectionRepository.save(collection);
        return mapEntityToDto(savedCollection);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialCollectionDto getMaterialCollectionById(Long id) {
        MaterialCollection collection = materialCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", id));
        return mapEntityToDto(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaterialCollectionDto> getAllMaterialCollections(Pageable pageable) {
        return materialCollectionRepository.findAll(pageable).map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public MaterialCollectionDto updateMaterialCollection(Long id, MaterialCollectionDto collectionDto) {
        MaterialCollection collection = materialCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", id));

        if (collectionDto.getName() != null && !collection.getName().equals(collectionDto.getName()) &&
            materialCollectionRepository.existsByName(collectionDto.getName())) {
            throw new BadRequestException("MaterialCollection name '" + collectionDto.getName() + "' is already taken.");
        }
        
        // Update basic fields
        mapDtoToEntityForUpdate(collectionDto, collection);

        // Handle material updates
        if (collectionDto.getMaterialIds() != null) {
            Set<Material> materials = new HashSet<>();
            if (!collectionDto.getMaterialIds().isEmpty()) {
                 materials.addAll(materialRepository.findAllById(collectionDto.getMaterialIds()));
                 if (materials.size() != collectionDto.getMaterialIds().size()) {
                    throw new BadRequestException("One or more material IDs provided for update were not found.");
                }
            }
            collection.setMaterials(materials); // Replace existing materials with the new set
        }
        // If materialIds is null, materials are not changed. If empty, all materials are removed.


        MaterialCollection updatedCollection = materialCollectionRepository.save(collection);
        return mapEntityToDto(updatedCollection);
    }

    @Override
    @Transactional
    public void deleteMaterialCollection(Long id) {
        if (!materialCollectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("MaterialCollection", "id", id);
        }
        // The @ManyToMany relationship with CascadeType.PERSIST and CascadeType.MERGE
        // on `materials` in MaterialCollection means deleting a collection
        // will remove entries from `collection_materials` but not delete Material entities themselves.
        // If materials should be disassociated (which is default for ManyToMany owner removal), this is fine.
        materialCollectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MaterialCollectionDto addMaterialToCollection(Long collectionId, Long materialId) {
        MaterialCollection collection = materialCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", collectionId));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", materialId));

        if (collection.getMaterials().contains(material)) {
            throw new BadRequestException("Material already exists in the collection.");
        }
        collection.addMaterial(material); // Use convenience method if it handles both sides
        // collection.getMaterials().add(material); // Or add directly
        materialCollectionRepository.save(collection);
        return mapEntityToDto(collection);
    }

    @Override
    @Transactional
    public MaterialCollectionDto removeMaterialFromCollection(Long collectionId, Long materialId) {
        MaterialCollection collection = materialCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialCollection", "id", collectionId));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", materialId));

        if (!collection.getMaterials().contains(material)) {
            throw new BadRequestException("Material does not exist in the collection.");
        }
        collection.removeMaterial(material); // Use convenience method if it handles both sides
        // collection.getMaterials().remove(material); // Or remove directly
        materialCollectionRepository.save(collection);
        return mapEntityToDto(collection);
    }

    // --- Helper Mappers ---
    private MaterialCollectionDto mapEntityToDto(MaterialCollection collection) {
        if (collection == null) return null;
        MaterialCollectionDto dto = new MaterialCollectionDto();
        BeanUtils.copyProperties(collection, dto, "creator", "materials");

        if (collection.getCreator() != null) {
            dto.setCreatorId(collection.getCreator().getId());
            dto.setCreatorUsername(collection.getCreator().getUsername());
        }
        if (collection.getMaterials() != null) {
            dto.setMaterials(collection.getMaterials().stream()
                    .map(this::mapMaterialEntityToDto)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private void mapDtoToEntity(MaterialCollectionDto dto, MaterialCollection entity) {
        // Exclude id, creator, materials, createdAt, updatedAt (handled separately or by JPA)
        BeanUtils.copyProperties(dto, entity, "id", "creatorId", "creatorUsername", "materials", "materialIds", "createdAt", "updatedAt");
    }
    
    private void mapDtoToEntityForUpdate(MaterialCollectionDto dto, MaterialCollection entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getThemeOrRoomId() != null) entity.setThemeOrRoomId(dto.getThemeOrRoomId());
        if (dto.getSeason() != null) entity.setSeason(dto.getSeason());
        // creatorId should not change typically in an update
        // materialIds are handled separately in the update method
    }


    private MaterialDto mapMaterialEntityToDto(Material material) { // Helper for nested DTO
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
