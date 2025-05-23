package com.example.authservice.service;

import com.example.authservice.dto.RoleDto;
import com.example.authservice.dto.RoleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    RoleResponseDto createRole(RoleDto roleDto);
    RoleResponseDto getRoleById(Long id);
    RoleResponseDto getRoleByName(String name);
    Page<RoleResponseDto> getAllRoles(Pageable pageable);
    RoleResponseDto updateRole(Long id, RoleDto roleDto);
    void deleteRole(Long id);

    RoleResponseDto assignPermissionToRole(String roleName, String permissionName);
    RoleResponseDto removePermissionFromRole(String roleName, String permissionName);
}
