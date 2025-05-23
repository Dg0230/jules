package com.example.authservice.service;

import com.example.authservice.dto.MaterialCollectionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterialCollectionService {
    MaterialCollectionDto createMaterialCollection(MaterialCollectionDto collectionDto, Long creatorId);
    MaterialCollectionDto getMaterialCollectionById(Long id);
    Page<MaterialCollectionDto> getAllMaterialCollections(Pageable pageable);
    MaterialCollectionDto updateMaterialCollection(Long id, MaterialCollectionDto collectionDto); // creatorId not needed for update as ownership typically doesn't change
    void deleteMaterialCollection(Long id);

    MaterialCollectionDto addMaterialToCollection(Long collectionId, Long materialId);
    MaterialCollectionDto removeMaterialFromCollection(Long collectionId, Long materialId);
}
