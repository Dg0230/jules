package com.example.authservice.service;

import com.example.authservice.dto.MaterialDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaterialService {
    MaterialDto createMaterial(MaterialDto materialDto, Long uploaderId);
    MaterialDto getMaterialById(Long id);
    Page<MaterialDto> getAllMaterials(Pageable pageable);
    MaterialDto updateMaterial(Long id, MaterialDto materialDto, Long uploaderId); // uploaderId for validation if needed, or just for consistency
    void deleteMaterial(Long id);
}
