package com.example.authservice.service;

import com.example.authservice.dto.PermissionDto;
import com.example.authservice.dto.PermissionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {
    PermissionResponseDto createPermission(PermissionDto permissionDto);
    PermissionResponseDto getPermissionById(Long id);
    PermissionResponseDto getPermissionByName(String name);
    Page<PermissionResponseDto> getAllPermissions(Pageable pageable);
    PermissionResponseDto updatePermission(Long id, PermissionDto permissionDto);
    void deletePermission(Long id);
}
