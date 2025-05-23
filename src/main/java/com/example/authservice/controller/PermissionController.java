package com.example.authservice.controller;

import com.example.authservice.dto.PermissionDto;
import com.example.authservice.dto.PermissionResponseDto;
import com.example.authservice.dto.SuccessResponseDto;
import com.example.authservice.service.PermissionService;
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
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponseDto> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        PermissionResponseDto createdPermission = permissionService.createPermission(permissionDto);
        return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PermissionResponseDto>> getAllPermissions(@PageableDefault(size = 20) Pageable pageable) {
        Page<PermissionResponseDto> permissions = permissionService.getAllPermissions(pageable);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PermissionResponseDto> getPermissionById(@PathVariable Long id) {
        PermissionResponseDto permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponseDto> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionDto permissionDto) {
        PermissionResponseDto updatedPermission = permissionService.updatePermission(id, permissionDto);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDto<Object>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(new SuccessResponseDto<>("Permission deleted successfully with id: " + id));
    }
}
