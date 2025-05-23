package com.example.authservice.service.impl;

import com.example.authservice.dto.PermissionDto;
import com.example.authservice.dto.PermissionResponseDto;
import com.example.authservice.entity.Permission;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.PermissionRepository;
import com.example.authservice.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public PermissionResponseDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByName(permissionDto.getName())) {
            throw new BadRequestException("Permission name '" + permissionDto.getName() + "' is already taken.");
        }
        Permission permission = new Permission();
        permission.setName(permissionDto.getName());
        permission.setDescription(permissionDto.getDescription());

        Permission savedPermission = permissionRepository.save(permission);
        return mapPermissionToPermissionResponseDto(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponseDto getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return mapPermissionToPermissionResponseDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponseDto getPermissionByName(String name) {
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", name));
        return mapPermissionToPermissionResponseDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponseDto> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(this::mapPermissionToPermissionResponseDto);
    }

    @Override
    @Transactional
    public PermissionResponseDto updatePermission(Long id, PermissionDto permissionDto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        // Check if new permission name is taken by another permission
        if (!permission.getName().equals(permissionDto.getName()) && permissionRepository.existsByName(permissionDto.getName())) {
            throw new BadRequestException("Permission name '" + permissionDto.getName() + "' is already taken by another permission.");
        }

        permission.setName(permissionDto.getName());
        permission.setDescription(permissionDto.getDescription());

        Permission updatedPermission = permissionRepository.save(permission);
        return mapPermissionToPermissionResponseDto(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        // Similar to Role deletion, consider implications.
        // Permission is on the inverse side of Role-Permission relationship.
        // Deleting a permission should update the role_permissions join table.
        permissionRepository.delete(permission);
    }

    // --- Helper DTO Mapper ---
    private PermissionResponseDto mapPermissionToPermissionResponseDto(Permission permission) {
        if (permission == null) return null;
        return new PermissionResponseDto(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
