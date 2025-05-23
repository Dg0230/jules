package com.example.authservice.service.impl;

import com.example.authservice.dto.RoleDto;
import com.example.authservice.dto.RoleResponseDto;
import com.example.authservice.entity.Permission;
import com.example.authservice.entity.Role;
import com.example.authservice.exception.BadRequestException;
import com.example.authservice.exception.ResourceNotFoundException;
import com.example.authservice.repository.PermissionRepository;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // For CollectionUtils.isEmpty

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new BadRequestException("Role name '" + roleDto.getName() + "' is already taken.");
        }
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());

        if (!CollectionUtils.isEmpty(roleDto.getPermissions())) {
            Set<Permission> permissions = roleDto.getPermissions().stream()
                    .map(permissionName -> permissionRepository.findByName(permissionName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        } else {
            role.setPermissions(new HashSet<>());
        }

        Role savedRole = roleRepository.save(role);
        return mapRoleToRoleResponseDto(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapRoleToRoleResponseDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
        return mapRoleToRoleResponseDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponseDto> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::mapRoleToRoleResponseDto);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if new role name is taken by another role
        if (!role.getName().equals(roleDto.getName()) && roleRepository.existsByName(roleDto.getName())) {
            throw new BadRequestException("Role name '" + roleDto.getName() + "' is already taken by another role.");
        }

        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());

        if (roleDto.getPermissions() != null) { // Allows clearing permissions if an empty set is passed
            Set<Permission> permissions = roleDto.getPermissions().stream()
                    .map(permissionName -> permissionRepository.findByName(permissionName)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        // If roleDto.getPermissions() is null, existing permissions are not changed.

        Role updatedRole = roleRepository.save(role);
        return mapRoleToRoleResponseDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        // Consider implications: what happens to users with this role?
        // JPA will remove entries from user_roles and role_permissions due to @JoinTable and cascade (if any, but default is none for ManyToMany from owning side)
        // If there are constraints or specific logic needed (e.g., cannot delete role if users are assigned), add checks here.
        // For now, direct deletion.
        
        // Manually break relationships with users to avoid issues if cascade is not set up from User side for roles.
        // This is important because Role is likely the inverse side of User-Role relationship.
        // However, our User entity defines the @JoinTable, making it the owner.
        // So, removing a Role should correctly update the join table.
        // Similar for Role-Permission, Role is the owner.
        
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleResponseDto assignPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName));

        if (role.getPermissions().contains(permission)) {
            throw new BadRequestException("Role '" + roleName + "' already has permission: " + permissionName);
        }
        role.getPermissions().add(permission);
        roleRepository.save(role);
        return mapRoleToRoleResponseDto(role);
    }

    @Override
    @Transactional
    public RoleResponseDto removePermissionFromRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", permissionName));

        if (!role.getPermissions().contains(permission)) {
            throw new BadRequestException("Role '" + roleName + "' does not have permission: " + permissionName);
        }
        role.getPermissions().remove(permission);
        roleRepository.save(role);
        return mapRoleToRoleResponseDto(role);
    }

    // --- Helper DTO Mapper ---
    private RoleResponseDto mapRoleToRoleResponseDto(Role role) {
        if (role == null) return null;
        return new RoleResponseDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
        );
    }
}
