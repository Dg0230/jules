package com.example.authservice.controller;

import com.example.authservice.dto.RoleDto;
import com.example.authservice.dto.RoleResponseDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleResponseDto createdRole = roleService.createRole(roleDto);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RoleResponseDto>> getAllRoles(@PageableDefault(size = 20) Pageable pageable) {
        Page<RoleResponseDto> roles = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        RoleResponseDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        RoleResponseDto updatedRole = roleService.updateRole(id, roleDto);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Role deleted successfully with id: " + id));
    }

    @PostMapping("/{roleName}/permissions/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> assignPermissionToRole(@PathVariable String roleName, @PathVariable String permissionName) {
        RoleResponseDto updatedRole = roleService.assignPermissionToRole(roleName, permissionName);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{roleName}/permissions/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> removePermissionFromRole(@PathVariable String roleName, @PathVariable String permissionName) {
        RoleResponseDto updatedRole = roleService.removePermissionFromRole(roleName, permissionName);
        return ResponseEntity.ok(updatedRole);
    }
}
